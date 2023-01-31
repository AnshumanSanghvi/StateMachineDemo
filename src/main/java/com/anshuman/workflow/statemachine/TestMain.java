package com.anshuman.workflow.statemachine;

import static com.anshuman.workflow.statemachine.data.constant.TestConstant.STATE_MACHINE_NAME;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_INITIALIZE;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_SUBMIT;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_COMPLETE;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_FLOW_JUNCTION;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_REVIEW;

import com.anshuman.workflow.statemachine.builder.TestSMBuilder;
import com.anshuman.workflow.statemachine.event.TestEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.state.TestState;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public class TestMain {

    public static void main(String[] args) {
        int reviewersCount = 3;
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(reviewersCount);
        reviewerMap.put(1, 234L);
        reviewerMap.put(2, 123L);
        reviewerMap.put(3, 235L);
        boolean isParallel = false;
        int maxChangeRequests = 3;
        int maxRollBackCount = 3;
        StateMachine<TestState, TestEvent> sm = createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        EventSendHelper.sendEvents(sm, E_INITIALIZE, E_SUBMIT, E_TRIGGER_REVIEW, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.sendForwardEvent(sm, 1, 234L);
        EventSendHelper.sendForwardEvent(sm, 2, 123L);
        EventSendHelper.sendForwardEvent(sm, 3, 235L);
        EventSendHelper.sendEvent(sm, E_TRIGGER_COMPLETE);
        log.info("Final SM TestState: {}, ExtendedState: [{}]",
            sm.getState().getId(),
            sm.getExtendedState().getVariables().entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> (String) entry.getKey()))
                .map(entry -> entry.getKey() + " : " + entry.getValue())
                .collect(Collectors.joining(", ")));
    }

    public static StateMachine<TestState, TestEvent> createStateMachine(Map<Integer, Long> reviewerMap, boolean isParallel, int maxChangeRequests,
        int maxRollBackCount) {
        try {
            int reviewerCount = reviewerMap.size();
            StateMachine<TestState, TestEvent> sm = TestSMBuilder.createStateMachine(STATE_MACHINE_NAME,
                reviewerCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);
            sm.startReactively().block();
            log.info("starting statemachine: {}", sm.getId());
            return sm;
        } catch (Exception ex) {
            throw new StateMachineException("Exception encountered in creating state machine", ex);
        }
    }

}
