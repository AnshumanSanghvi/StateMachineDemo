package com.sttl.hrms.workflow.statemachine.leaveapp;

import static com.sttl.hrms.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_CLOSED_STATE_TYPE;
import static com.sttl.hrms.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_FORWARDED_COUNT;
import static com.sttl.hrms.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_FORWARDED_MAP;
import static com.sttl.hrms.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;
import static com.sttl.hrms.workflow.statemachine.data.constant.LeaveAppSMConstants.VAL_APPROVED;
import static com.sttl.hrms.workflow.statemachine.data.constant.StateMachineConstants.KEY_RETURN_COUNT;
import static com.sttl.hrms.workflow.statemachine.data.constant.StateMachineConstants.KEY_ROLL_BACK_COUNT;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_FORWARD;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_ROLL_BACK;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_SUBMIT;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_COMPLETE;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_FLOW_JUNCTION;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_REVIEW_OF;
import static com.sttl.hrms.workflow.statemachine.util.ExtendedStateHelper.getInt;
import static com.sttl.hrms.workflow.statemachine.util.ExtendedStateHelper.getMap;
import static com.sttl.hrms.workflow.statemachine.util.ExtendedStateHelper.getString;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sttl.hrms.workflow.statemachine.data.Pair;
import com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.state.LeaveAppState;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Slf4j
class LeaveAppStateMachineTest {

    private StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

    private static StateMachine<LeaveAppState, LeaveAppEvent> createStateMachine(Map<Integer, Long> reviewerMap, boolean isParallel,
        int maxChangeRequests,
        int maxRollBackCount) {
        try {
            int reviewerCount = reviewerMap.size();
            StateMachine<LeaveAppState, LeaveAppEvent> sm = LeaveAppSMBuilder
                .createStateMachine(LEAVE_APP_WF_V1, reviewerCount, reviewerMap,
                    isParallel, maxChangeRequests, maxRollBackCount);
            sm.startReactively().block();
            log.info("starting statemachine: {}", sm.getId());
            return sm;
        } catch (Exception ex) {
            throw new StateMachineException("Exception encountered in creating state machine", ex);
        }
    }

    @Test
    void testSerialApproval() {
        // given
        int reviewersCount = 3;
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(reviewersCount);
        reviewerMap.put(1, 234L);
        reviewerMap.put(2, 123L);
        reviewerMap.put(3, 235L);
        boolean isParallel = false;
        int maxChangeRequests = 3;
        int maxRollBackCount = 3;
        stateMachine = createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        // when
        EventSendHelper.sendEvents(stateMachine, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION).blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 1, 234L, "").blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 2, 123L, "").blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 3, 235L, "").blockLast();
        EventSendHelper.sendEvent(stateMachine, E_TRIGGER_COMPLETE).blockLast();

        // then
        ExtendedState extendedState = stateMachine.getExtendedState();
        Assertions.assertTrue(stateMachine.isComplete());
        Map<Integer, Pair<Long, Boolean>> expectedForwardMap =
            Map.of(1, new Pair<>(234L, true), 2, new Pair<>(123L, true), 3, new Pair<>(235L, true));
        assertForwardMap(extendedState, expectedForwardMap);
        int expectedReturnCount = 0;
        assertReturnCount(extendedState, expectedReturnCount);
        assertClosedStateType(extendedState, VAL_APPROVED);
        int expectedForwardedCount = 3;
        assertForwardedCount(extendedState, expectedForwardedCount);
        int expectedRollBackCount = 0;
        assertRollBackApprovalCount(extendedState, expectedRollBackCount);
    }

    @Test
    void testSerialRollBackAndReturn() {
        // given
        int reviewersCount = 3;
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(reviewersCount);
        reviewerMap.put(1, 234L);
        reviewerMap.put(2, 123L);
        reviewerMap.put(3, 235L);
        boolean isParallel = false;
        int maxChangeRequests = 3;
        int maxRollBackCount = 3;
        stateMachine = createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        // when
        EventSendHelper.sendEvents(stateMachine, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION).blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 1, 234L, "").blockLast();
        EventSendHelper.sendRollBackApprovalEvent(stateMachine, E_ROLL_BACK, 1, 234L).blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 1, 234L, "").blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 2, 123L, "").blockLast();
        EventSendHelper.sendForwardEvent(stateMachine, E_FORWARD, 3, 235L, "").blockLast();
        EventSendHelper.sendEvent(stateMachine, E_TRIGGER_COMPLETE).blockLast();

        // then
        ExtendedState extendedState = stateMachine.getExtendedState();
        Assertions.assertTrue(stateMachine.isComplete());
        Map<Integer, Pair<Long, Boolean>> expectedForwardMap =
            Map.of(1, new Pair<>(234L, true), 2, new Pair<>(123L, true), 3, new Pair<>(235L, true));
        assertForwardMap(extendedState, expectedForwardMap);
        int expectedReturnCount = 0;
        assertReturnCount(extendedState, expectedReturnCount);
        assertClosedStateType(extendedState, VAL_APPROVED);
        int expectedForwardedCount = 3;
        assertForwardedCount(extendedState, expectedForwardedCount);
        int expectedRollBackCount = 1;
        assertRollBackApprovalCount(extendedState, expectedRollBackCount);
    }

    private static void assertReturnCount(ExtendedState extendedState, int expectedReturnCount) {
        int actualReturnCount = getInt(extendedState, KEY_RETURN_COUNT, -1);
        assertEquals(expectedReturnCount, actualReturnCount, "return count does not match");
    }

    private static void assertForwardedCount(ExtendedState extendedState, int expectedForwardedCount) {
        int actualForwardedCount = getInt(extendedState, KEY_FORWARDED_COUNT, -1);
        assertEquals(expectedForwardedCount, actualForwardedCount, "forwarded count does not match");
    }

    private static void assertRollBackApprovalCount(ExtendedState extendedState, int expectedRollBackCount) {
        int actualRollBackCount = getInt(extendedState, KEY_ROLL_BACK_COUNT, -1);
        assertEquals(expectedRollBackCount, actualRollBackCount, "roll back approval count does not match");
    }

    private static void assertForwardMap(ExtendedState extendedState, Map<Integer, Pair<Long, Boolean>> expectedForwardMap) {
        Map<Integer, Pair<Long, Boolean>> actualForwardMap = getMap(extendedState, KEY_FORWARDED_MAP, emptyMap());
        assertEquals(expectedForwardMap, actualForwardMap, "forward map does not match");
    }

    private static void assertClosedStateType(ExtendedState extendedState, String expectedClosedStateType) {
        String actualClosedStateType = getString(extendedState, KEY_CLOSED_STATE_TYPE, "");
        assertEquals(expectedClosedStateType, actualClosedStateType, "Closed state does not match.");
    }


}
