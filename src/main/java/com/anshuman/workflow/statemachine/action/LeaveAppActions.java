package com.anshuman.workflow.statemachine.action;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.*;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_APPROVE;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_FORWARD;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_COMPLETE;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getBoolean;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getInt;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getMap;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getPair;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getStateId;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getString;

import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import com.anshuman.workflow.statemachine.util.EventResultHelper;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import com.anshuman.workflow.statemachine.util.ExtendedStateHelper;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

/**
 * Order of execution for transition from A -> B: <br>
 * - two states A and B <br>
 * - state entry and exit action on A <br>
 * - state entry and exit action on B <br>
 * - transition action on the A -> B transition <br>
 */
@Slf4j
public class LeaveAppActions {

    private LeaveAppActions() {
        // use class statically
    }

    /**
     * State Actions go here
     */
    public static class StateActions {

        private StateActions() {
            // use class statically
        }

        public static void initial(StateContext<LeaveAppState, LeaveAppEvent> context, int reviewers, Map<Integer, Long> reviewerMap,
            boolean isParallel, int maxChangeRequests, int maxRollBackCount) {
            initial(context.getStateMachine(), reviewers, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);
        }

        public static void initial(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine, int reviewers, Map<Integer, Long> reviewerMap,
            boolean isParallel, int maxChangeRequests, int maxRollBackCount) {
            String stateId = Optional
                .ofNullable(stateMachine)
                .flatMap(sm -> Optional.ofNullable(sm.getState())
                    .map(State::getId)
                    .map(Object::toString))
                .orElse("null");
            log.trace("Executing action: initializeStateAction with currentState: {}", stateId);

            Optional.ofNullable(stateMachine)
                .map(StateMachine::getExtendedState)
                .flatMap(exs -> Optional.ofNullable(exs.getVariables()))
                .ifPresent(map -> {
                    map.put(KEY_ROLL_BACK_MAX, maxRollBackCount);
                    map.put(KEY_ROLL_BACK_COUNT, 0);
                    map.put(KEY_RETURN_COUNT, 0);
                    map.put(KEY_CLOSED_STATE_TYPE, "");
                    map.put(KEY_APPROVAL_FLOW_TYPE, isParallel ? VAL_PARALLEL : VAL_SERIAL);
                    map.put(KEY_REVIEWERS_COUNT, reviewers);
                    map.put(KEY_MAX_CHANGE_REQUESTS, maxChangeRequests);
                    map.put(KEY_REVIEWERS_MAP, reviewerMap);
                    map.put(KEY_FORWARDED_MAP, reviewerMap.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey,
                            entry -> new Pair<>(entry.getValue(), false))));

                    ExtendedState extendedState = stateMachine.getExtendedState();
                    log.trace("Setting extended state- rollbackCount: {}, returnCount: {}, closedState: {}, reviewersCount: {}, "
                            + "reviewersList: {}",
                        getInt(extendedState, KEY_ROLL_BACK_COUNT, 0),
                        getInt(extendedState, KEY_RETURN_COUNT, 0),
                        getString(extendedState, KEY_CLOSED_STATE_TYPE, ""),
                        getInt(extendedState, KEY_REVIEWERS_COUNT, 0),
                        map.get(KEY_REVIEWERS_MAP));
                });
        }
    }

    public static class TransitionActions {

        private TransitionActions() {
            // use class statically
        }

        public static void forward(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: forwardStateExitAction with currentState: {}", getStateId(context));

            // if reviewers can forward the application in any order, then auto-approve.
            if (getBoolean(context, KEY_ANY_APPROVE)) {
                triggerApproveEvent(context);
            }
            // else record the forward event by the reviewer, keeping their order intact.
            else {
                manualApproveOnForward(context);
            }
        }

        private static void manualApproveOnForward(StateContext<LeaveAppState, LeaveAppEvent> context) {

            // get the relevant key matching the forwardBy from the forward Map.
            Pair<Integer, Long> forwardBy = getPair(context, KEY_LAST_FORWARDED_BY);
            Map<Integer, Pair<Long, Boolean>> forwardMap = getMap(context, KEY_FORWARDED_MAP);
            Predicate<Entry<Integer, Pair<Long, Boolean>>> reviewerIdPresent = entry -> entry.getValue().getFirst().equals(forwardBy.getSecond());
            Predicate<Entry<Integer, Pair<Long, Boolean>>> orderIdPresent = entry -> entry.getKey().equals(forwardBy.getFirst());
            Integer forwardOrderKey = forwardMap
                .entrySet()
                .stream()
                .filter(reviewerIdPresent.and(orderIdPresent))
                .findFirst()
                .map(Entry::getKey)
                .orElse(null);

            // throw error if no key found.
            if (forwardOrderKey == null) {
                String errorMsg = "Could not forward the application by the given reviewer with id: " +
                    forwardBy.getSecond() + " and order: " + forwardBy.getFirst() +
                    " as the application was either already forwarded, " +
                    " or the reviewerId is not present in the list of reviewers";
                log.error(errorMsg);
                context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                return;
            }

            // continue the forward logic if key found.
            // 1. set the value in the forward map
            forwardMap.put(forwardOrderKey, new Pair<>(forwardBy.getSecond(), true));

            // 2. increment the forward count,
            int forwardedCount = getInt(context, KEY_FORWARDED_COUNT) + 1;
            int reviewersCount = getInt(context, KEY_REVIEWERS_COUNT);
            log.debug("forwardedCount: {}, reviewersCount: {}", forwardedCount, reviewersCount);
            Map<Object, Object> map = context.getExtendedState().getVariables();
            map.put(KEY_FORWARDED_COUNT, forwardedCount);

            // 3. if all reviewers have forwarded the application then approve it
            if (forwardedCount == reviewersCount) {
                triggerApproveEvent(context);
            }
        }

        public static void approve(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: approveTransitionAction with currentState: {}", getStateId(context));
            Map<Object, Object> map = context.getExtendedState().getVariables();
            map.put(KEY_CLOSED_STATE_TYPE, VAL_APPROVED);
            log.trace("Setting extended state- closedState: {}", getString(context, KEY_CLOSED_STATE_TYPE));
        }

        public static void requestChanges(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: requestChangesTransitionAction with currentState: {}", getStateId(context));
            Map<Object, Object> map = context.getExtendedState().getVariables();
            map.put(KEY_RETURN_COUNT, getInt(context, KEY_RETURN_COUNT) + 1);
            map.put(KEY_FORWARDED_COUNT, 0);
            map.put(KEY_ROLL_BACK_COUNT, 0);
            log.trace("Setting extended state- returnCount: {}, forwardCount: {}, rollBackCount: {}",
                getInt(context, KEY_RETURN_COUNT), getInt(context, KEY_FORWARDED_COUNT),
                getInt(context, KEY_ROLL_BACK_COUNT));
        }

        public static void reject(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: rejectTransitionAction with currentState: {}", getStateId(context));
            Map<Object, Object> map = context.getExtendedState().getVariables();
            map.put(KEY_CLOSED_STATE_TYPE, VAL_REJECTED);
            log.trace("Setting extended state- closedState: {}", getString(context, KEY_CLOSED_STATE_TYPE));
        }

        public static void cancel(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: cancelTransitionAction with currentState: {}", getStateId(context));
            Map<Object, Object> map = context.getExtendedState().getVariables();
            map.put(KEY_CLOSED_STATE_TYPE, VAL_CANCELED);
            log.trace("Setting extended state- closedState: {}", getString(context, KEY_CLOSED_STATE_TYPE));
            autoTriggerComplete(context);
        }

        public static void autoTriggerComplete(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: autoTriggerCompleteAction with currentState: {}", getStateId(context));
            var resultFlux = EventSendHelper.sendEvent(context.getStateMachine(), E_TRIGGER_COMPLETE);
            String results = "[" + EventResultHelper.toResultDTOString(resultFlux) + "]";
            log.debug("autoTriggerCompleteAction results: {}", results);
        }

        public static void rollBackApproval(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: rollBackTransitionAction with currentState: {}", getStateId(context));
            Map<Object, Object> map = context.getExtendedState().getVariables();
            // increment roll back count
            map.put(KEY_ROLL_BACK_COUNT, getInt(context, KEY_ROLL_BACK_COUNT) + 1);

            // decrease forward count, don't let forward count be negative.
            map.put(KEY_FORWARDED_COUNT, Math.max((getInt(context, KEY_FORWARDED_COUNT) - 1), 0));

            // reset null state
            map.put(KEY_CLOSED_STATE_TYPE, null);

            // reset approve by
            map.put(KEY_APPROVE_BY, null);

            // reset forwarded by
            Pair<Integer, Long> forwardedBy = ExtendedStateHelper.getPair(context, KEY_LAST_FORWARDED_BY);
            map.put(KEY_LAST_FORWARDED_BY, null);

            // reset last entry in forwarded Map
            Map<Integer, Pair<Long, Boolean>> forwardedMap = ExtendedStateHelper.getMap(context.getExtendedState(), KEY_FORWARDED_MAP, Collections.emptyMap());
            forwardedMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(forwardedBy.getFirst()))
                .filter(entry -> entry.getValue().getFirst().equals(forwardedBy.getSecond()))
                .findFirst()
                .ifPresent(entry -> entry.getValue().setSecond(Boolean.FALSE));

            log.trace("Setting extended state- rollBackCount: {}", getInt(context, KEY_ROLL_BACK_COUNT));
        }

        public static void triggerApproveEvent(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: autoApproveTransitionAction with currentState: {}", getStateId(context));
            var stateMachine = context.getStateMachine();
            Pair<Integer, Long> forwardBy = ExtendedStateHelper.getPair(context, KEY_LAST_FORWARDED_BY);
            var result = EventSendHelper.sendApproveEvent(stateMachine, E_APPROVE, forwardBy.getFirst(), forwardBy.getSecond());

            log.debug("autoApproveTransitionAction results: {}", result
                .toStream()
                .map(EventResultDTO::new)
                .map(EventResultDTO::toString)
                .collect(Collectors.joining(", ")));
        }

        public static void approveInParallelFlow(StateContext<LeaveAppState, LeaveAppEvent> context) {
            log.trace("Executing action: approveInParallelFlowTransitionAction with currentState: {}", getStateId(context));
            var map = context.getExtendedState().getVariables();
            map.put(KEY_CLOSED_STATE_TYPE, VAL_APPROVED);
        }

        public static void forwardChoice(StateContext<LeaveAppState, LeaveAppEvent> context) {
            var map = context.getExtendedState().getVariables();
            int forwardedCount = (Integer) map.getOrDefault(KEY_FORWARDED_COUNT, 0);
            int reviewerCount = (Integer) map.getOrDefault(KEY_REVIEWERS_COUNT, 0);
            log.info("forwardChoice - forwardedCount: {}, reviewerCount: {}", forwardedCount, reviewerCount);
            if (forwardedCount < reviewerCount) {
                StateMachine<LeaveAppState, LeaveAppEvent> sm = context.getStateMachine();
                EventSendHelper.sendEvents(sm, E_FORWARD);
            } else if (forwardedCount == reviewerCount) {
                StateMachine<LeaveAppState, LeaveAppEvent> sm = context.getStateMachine();
                EventSendHelper.sendEvents(sm, E_APPROVE);
            } else {
                throw new StateMachineException("forwardCount: " + forwardedCount + " higher than reviewerCount: " + reviewerCount);
            }
        }
    }

}
