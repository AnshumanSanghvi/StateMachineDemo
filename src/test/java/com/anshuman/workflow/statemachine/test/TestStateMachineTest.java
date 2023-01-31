package com.anshuman.workflow.statemachine.test;

import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_CLOSED_STATE_TYPE;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_FORWARDED_COUNT;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_FORWARDED_MAP;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_RETURN_COUNT;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_ROLL_BACK_COUNT;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.VAL_APPROVED;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_INITIALIZE;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_SUBMIT;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_COMPLETE;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_FLOW_JUNCTION;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_TRIGGER_REVIEW;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getInt;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getMap;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getString;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anshuman.workflow.statemachine.TestMain;
import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.event.TestEvent;
import com.anshuman.workflow.statemachine.state.TestState;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TestStateMachineTest {

    private StateMachine<TestState, TestEvent> stateMachine;

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
        stateMachine = TestMain.createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        // when
        EventSendHelper.sendEvents(stateMachine, E_INITIALIZE, E_SUBMIT, E_TRIGGER_REVIEW, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.sendForwardEvent(stateMachine, 1, 234L);
        EventSendHelper.sendForwardEvent(stateMachine, 2, 123L);
        EventSendHelper.sendForwardEvent(stateMachine, 3, 235L);
        EventSendHelper.sendEvent(stateMachine, E_TRIGGER_COMPLETE);

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
        stateMachine = TestMain.createStateMachine(reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        // when
        EventSendHelper.sendEvents(stateMachine, E_INITIALIZE, E_SUBMIT, E_TRIGGER_REVIEW, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.sendForwardEvent(stateMachine, 1, 234L);
        EventSendHelper.sendRollBackApprovalEvent(stateMachine, 1, 234L);
        EventSendHelper.sendForwardEvent(stateMachine, 1, 234L);
        EventSendHelper.sendForwardEvent(stateMachine, 2, 123L);
        EventSendHelper.sendForwardEvent(stateMachine, 3, 235L);
        EventSendHelper.sendEvent(stateMachine, E_TRIGGER_COMPLETE);

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
