package com.sttl.hrms.workflow.statemachine;

import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity.WorkflowProperties;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.sttl.hrms.workflow.statemachine.ExtStateUtil.get;
import static com.sttl.hrms.workflow.statemachine.ExtStateUtil.getStateId;
import static com.sttl.hrms.workflow.statemachine.SMConstants.*;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_APPROVE;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_COMPLETE;
import static java.util.stream.Collectors.toMap;

/**
 * Order of execution for transition from A -> B: <br>
 * - two states A and B <br>
 * - state entry and exit action on A <br>
 * - state entry and exit action on B <br>
 * - transition action on the A -> B transition <br>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class Actions {

    private Actions() {
        // use class statically
    }

    public static void initial(StateContext<String, String> context, WorkflowProperties workflowProperties, Integer reviewers,
            Map<Integer, Long> reviewerMap, boolean isParallel, Integer maxChangeRequests, Integer maxRollBackCount) {
        initial(context.getStateMachine(), workflowProperties, reviewers, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);
    }

    public static void initial(StateMachine<String, String> stateMachine, WorkflowProperties workflowProperties, Integer reviewers,
            Map<Integer, Long> reviewerMap, Boolean isParallel, Integer maxChangeRequests, Integer maxRollBackCount) {

        String stateId = Optional.ofNullable(stateMachine).flatMap(sm -> Optional.ofNullable(sm.getState())
                .map(State::getId).map(Object::toString)).orElse("null");
        log.trace("Executing action: initialize stateMachine state with currentState: {}", stateId);

        Optional.ofNullable(stateMachine)
                .map(StateMachine::getExtendedState)
                .flatMap(exs -> Optional.ofNullable(exs.getVariables()))
                .ifPresent(map -> setExtendedState(stateMachine, workflowProperties, reviewers, reviewerMap, isParallel,
                        maxChangeRequests, maxRollBackCount, map));
    }

    private static void setExtendedState(StateMachine<String, String> stateMachine, WorkflowProperties workflowProperties,
            Integer reviewers, Map<Integer, Long> reviewerMap, Boolean isParallel, Integer maxChangeRequests,
            Integer maxRollBackCount, Map<Object, Object> map) {
        ExtendedState extState = stateMachine.getExtendedState();

        var defaultProps = workflowProperties == null ? new WorkflowProperties() : workflowProperties;

        // flow type property
        map.putIfAbsent(KEY_APPROVAL_FLOW_TYPE, Optional.ofNullable(isParallel)
                .filter(Boolean::booleanValue).map(flow -> VAL_PARALLEL).orElse(VAL_SERIAL));

        // roll back properties
        map.putIfAbsent(KEY_ROLL_BACK_COUNT, 0);
        map.putIfAbsent(KEY_ROLL_BACK_MAX, Optional.ofNullable(maxRollBackCount).orElse(defaultProps.getRollbackMaxCount()));

        // request change / return properties
        map.putIfAbsent(KEY_RETURN_COUNT, 0);
        map.put(KEY_MAX_CHANGE_REQUESTS, Optional.ofNullable(maxChangeRequests).orElse(defaultProps.getChangeReqMaxCount()));

        // reviwer properties
        Optional.ofNullable(reviewers).ifPresent(rev -> map.putIfAbsent(KEY_REVIEWERS_COUNT, rev));
        Optional.ofNullable(reviewerMap).filter(Predicate.not(Map::isEmpty)).ifPresent(rmap -> map.putIfAbsent(KEY_REVIEWERS_MAP, rmap));
        Optional.ofNullable(reviewerMap).filter(Predicate.not(Map::isEmpty)).ifPresent(rmap ->
                map.putIfAbsent(KEY_FORWARDED_MAP, rmap.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, entry -> new Pair<>(entry.getValue(), false)))));

        // default state properties.
        map.putIfAbsent(KEY_CLOSED_STATE_TYPE, "");

        log.trace("Setting extended state- rollbackCountMax: {}, changeRequestsMax: {}, flowType: {}, reviewersCount: {}, "
                        + "reviewersList: {}",
                extState.get(KEY_ROLL_BACK_MAX, Integer.class),
                extState.get(KEY_MAX_CHANGE_REQUESTS, Integer.class),
                extState.get(KEY_APPROVAL_FLOW_TYPE, String.class),
                extState.get(KEY_REVIEWERS_COUNT, Integer.class),
                map.get(KEY_REVIEWERS_MAP));
    }

    public static void forward(StateContext<String, String> context) {
        log.trace("Executing action: forwardStateExitAction with currentState: {}", getStateId(context));

        // if reviewers can forward the application in any order, then auto-approve.
        if (Boolean.TRUE.equals(get(context, KEY_ANY_APPROVE, Boolean.class, Boolean.FALSE))) {
            triggerApproveEvent(context);
        }
        // else record the forward event by the reviewer, keeping their order intact.
        else {
            manualApproveOnForwardSerialFlow(context);
        }
    }

    private static void manualApproveOnForwardSerialFlow(StateContext<String, String> context) {

        ExtendedState extState = context.getExtendedState();

        // get the relevant key matching the forwardBy from the forward Map.
        Pair<Integer, Long> forwardBy = (Pair<Integer, Long>) get(extState, KEY_LAST_FORWARDED_BY, Pair.class, null);
        Map<Integer, Pair<Long, Boolean>> forwardMap = (Map<Integer, Pair<Long, Boolean>>) get(extState, KEY_FORWARDED_MAP, Map.class, null);
        Predicate<Map.Entry<Integer, Pair<Long, Boolean>>> reviewerIdPresent = entry -> entry.getValue().getFirst().equals(forwardBy.getSecond());
        Predicate<Map.Entry<Integer, Pair<Long, Boolean>>> orderIdPresent = entry -> entry.getKey().equals(forwardBy.getFirst());
        Integer forwardOrderKey = forwardMap
                .entrySet()
                .stream()
                .filter(reviewerIdPresent.and(orderIdPresent))
                .findFirst()
                .map(Map.Entry::getKey)
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
        int forwardedCount = get(extState, KEY_FORWARDED_COUNT, Integer.class, 0) + 1;
        int reviewersCount = get(extState, KEY_REVIEWERS_COUNT, Integer.class, 0);
        log.debug("forwardedCount: {}, reviewersCount: {}", forwardedCount, reviewersCount);
        Map<Object, Object> map = extState.getVariables();
        map.put(KEY_FORWARDED_COUNT, forwardedCount);

        // 3. if all reviewers have forwarded the application then approve it
        if (forwardedCount == reviewersCount) {
            triggerApproveEvent(context);
        }
    }

    public static void approve(StateContext<String, String> context) {
        log.trace("Executing action: approveTransitionAction with currentState: {}", getStateId(context));
        ExtendedState extState = context.getExtendedState();
        Map<Object, Object> map = extState.getVariables();
        map.put(KEY_CLOSED_STATE_TYPE, VAL_APPROVED);
        log.trace("Setting extended state- closedState: {}", get(extState, KEY_CLOSED_STATE_TYPE, String.class, ""));
    }

    public static void requestChanges(StateContext<String, String> context) {
        ExtendedState extState = context.getExtendedState();

        log.trace("Executing action: requestChangesTransitionAction with currentState: {}", getStateId(context));
        Map<Object, Object> map = extState.getVariables();
        map.put(KEY_RETURN_COUNT, get(extState, KEY_RETURN_COUNT, Integer.class, 0) + 1);
        map.put(KEY_FORWARDED_COUNT, 0);
        map.put(KEY_ROLL_BACK_COUNT, 0);
        log.trace("Setting extended state- returnCount: {}, forwardCount: {}, rollBackCount: {}",
                get(extState, KEY_RETURN_COUNT, Integer.class, 0),
                get(extState, KEY_FORWARDED_COUNT, Integer.class, 0),
                get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
    }

    public static void reject(StateContext<String, String> context) {
        log.trace("Executing action: rejectTransitionAction with currentState: {}", getStateId(context));
        ExtendedState extState = context.getExtendedState();
        Map<Object, Object> map = extState.getVariables();
        map.put(KEY_CLOSED_STATE_TYPE, VAL_REJECTED);
        log.trace("Setting extended state- closedState: {}", get(extState, KEY_CLOSED_STATE_TYPE, String.class, ""));
    }

    public static void cancel(StateContext<String, String> context) {
        log.trace("Executing action: cancelTransitionAction with currentState: {}", getStateId(context));

        ExtendedState extState = context.getExtendedState();
        Map<Object, Object> map = extState.getVariables();
        map.put(KEY_CLOSED_STATE_TYPE, VAL_CANCELED);
        log.trace("Setting extended state- closedState: {}", get(extState, KEY_CLOSED_STATE_TYPE, String.class, ""));
    }

    public static void rollBackApproval(StateContext<String, String> context) {
        log.trace("Executing action: rollBackTransitionAction with currentState: {}", getStateId(context));

        ExtendedState extState = context.getExtendedState();

        Map<Object, Object> map = extState.getVariables();
        // increment roll back count
        int currentRollBackCount = get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0);
        map.put(KEY_ROLL_BACK_COUNT, currentRollBackCount + 1);

        // decrease forward count, don't let forward count be negative.
        int forwardCount = get(extState, KEY_FORWARDED_COUNT, Integer.class, 0);
        map.put(KEY_FORWARDED_COUNT, Math.max((forwardCount - 1), 0));

        // reset null state
        map.put(KEY_CLOSED_STATE_TYPE, "");

        // reset approve by
        map.put(KEY_APPROVE_BY, 0);

        // reset forwarded by
        Pair<Integer, Long> forwardedBy = (Pair<Integer, Long>) get(extState, KEY_LAST_FORWARDED_BY, Pair.class, null);
        map.put(KEY_LAST_FORWARDED_BY, 0);

        // reset last entry in forwarded Map
        Map<Integer, Pair<Long, Boolean>> forwardedMap = get(extState, KEY_FORWARDED_MAP, Map.class, Collections.emptyMap());
        forwardedMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(forwardedBy.getFirst()))
                .filter(entry -> entry.getValue().getFirst().equals(forwardedBy.getSecond()))
                .findFirst()
                .ifPresent(entry -> entry.getValue().setSecond(Boolean.FALSE));

        log.trace("Setting extended state- rollBackCount: {}", get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
    }

    public static void triggerApproveEvent(StateContext<String, String> context) {
        log.trace("Executing action: autoApproveTransitionAction with currentState: {}", getStateId(context));

        ExtendedState extState = context.getExtendedState();
        Pair<Integer, Long> forwardBy = (Pair<Integer, Long>) get(extState, KEY_LAST_FORWARDED_BY, Pair.class, null);
        String comment = get(extState, KEY_APPROVE_COMMENT, String.class, "");

        Map<String, Object> headersMap = new HashMap<>();
        Optional.ofNullable(forwardBy.getFirst()).ifPresent(ord -> headersMap.put(MSG_KEY_ORDER_NO, ord));
        Optional.ofNullable(forwardBy.getSecond()).ifPresent(actId -> headersMap.put(MSG_KEY_ACTION_BY, actId));
        headersMap.put(MSG_KEY_COMMENT, comment);

        var result = EventSendHelper.sendMessagesToSM(context.getStateMachine(), headersMap, E_APPROVE.name(),
                E_TRIGGER_COMPLETE.name());

        log.debug("autoApproveTransitionAction results: {}", result
                .toStream()
                .map(EventResultDto::new)
                .map(EventResultDto::toString)
                .collect(Collectors.joining(", ")));
    }

    public static void approveInParallelFlow(StateContext<String, String> context) {
        log.trace("Executing action: approveInParallelFlowTransitionAction with currentState: {}", getStateId(context));
        var map = context.getExtendedState().getVariables();
        map.put(KEY_CLOSED_STATE_TYPE, VAL_APPROVED);
    }

}
