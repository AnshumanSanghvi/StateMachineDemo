package com.anshuman.workflow.statemachine;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_FORWARD;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_INITIALIZE;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_SUBMIT;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_COMPLETE;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_FLOW_JUNCTION;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_REVIEW_OF;

import com.anshuman.workflow.statemachine.builder.LeaveAppSMBuilder;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import com.anshuman.workflow.statemachine.util.EventResultHelper;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public class LeaveAppMain {

    public static void main(String[] args) {
        int reviewersCount = 3;
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(reviewersCount);
        reviewerMap.put(1, 234L);
        reviewerMap.put(2, 123L);
        reviewerMap.put(3, 235L);
        boolean isParallel = false;
        int maxChangeRequests = 3;
        int maxRollBackCount = 3;
        StateMachine<LeaveAppState, LeaveAppEvent> sm = createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        log.info("result: {}", EventResultHelper.toResultDTOString(EventSendHelper.sendEvents(sm, E_INITIALIZE, E_SUBMIT, E_TRIGGER_REVIEW_OF,
            E_TRIGGER_FLOW_JUNCTION)));
        log.info("result: {}", EventResultHelper.toResultDTOString(EventSendHelper.sendForwardEvent(sm, E_FORWARD, 1, 234L)));
        log.info("result: {}", EventResultHelper.toResultDTOString(EventSendHelper.sendForwardEvent(sm, E_FORWARD, 2, 123L)));
        log.info("result: {}", EventResultHelper.toResultDTOString(EventSendHelper.sendForwardEvent(sm, E_FORWARD, 3, 235L)));
        log.info("result: {}", EventResultHelper.toResultDTOString(EventSendHelper.sendEvent(sm, E_TRIGGER_COMPLETE)));

        log.info("Final SM State: {}, ExtendedState: [{}]",
            sm.getState().getId(),
            sm.getExtendedState().getVariables().entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> (String) entry.getKey()))
                .map(entry -> entry.getKey() + " : " + entry.getValue())
                .collect(Collectors.joining(", ")));
    }

    public static StateMachine<LeaveAppState, LeaveAppEvent> createStateMachine(Map<Integer, Long> reviewerMap, boolean isParallel, int maxChangeRequests,
        int maxRollBackCount) {
        try {
            int reviewerCount = reviewerMap.size();
            StateMachine<LeaveAppState, LeaveAppEvent> sm = LeaveAppSMBuilder.createStateMachine(LEAVE_APP_WF_V1,
                reviewerCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);
            sm.startReactively().block();
            log.info("starting statemachine: {}", sm.getId());
            return sm;
        } catch (Exception ex) {
            throw new StateMachineException("Exception encountered in creating state machine", ex);
        }
    }

}
