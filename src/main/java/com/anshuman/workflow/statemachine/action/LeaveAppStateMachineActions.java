package com.anshuman.workflow.statemachine.action;

import com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;

/**
 * Order of execution for actions:
 * - for two states A and B
 * - first, state entry and exit actions on A get executed,
 * - then, state entry and exit actions on B get executed,
 * - then, transition action on the A -> B transition is executed.
 *
 */
@Slf4j
public class LeaveAppStateMachineActions {

    private LeaveAppStateMachineActions() {
        // use class statically
    }

    public static void initiateLeaveAppWorkflow(ExtendedState extendedState) {
        Map<Object, Object> map = extendedState.getVariables();

        // Parallel: If any approver approves the application, then it is approved, no other approval is required.
        // Serial: all the approvers need to approve the application in the order of their hierarchy.
        map.put(LeaveAppSMConstants.IS_PARALLEL, false);
        // is this the first time the application is in this state, or has application been rolled back?
        map.put(LeaveAppSMConstants.ROLL_BACK_COUNT, 0);
        // is this the first the application is in this state, or has application been returned for changes?
        map.put(LeaveAppSMConstants.RETURN_COUNT, 0);
        // for future use: can an approver send the application forward in the hierarchy without approval?
        map.put(LeaveAppSMConstants.ONLY_FORWARD_WITH_APPROVAL, true);
    }

    public static void returnBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(LeaveAppSMConstants.RETURN_COUNT, 0) + 1;
        map.put(LeaveAppSMConstants.RETURN_COUNT, newCount);
    }

    public static void rollBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(LeaveAppSMConstants.ROLL_BACK_COUNT, 0) + 1;
        map.put(LeaveAppSMConstants.ROLL_BACK_COUNT, newCount);
        map.put(LeaveAppSMConstants.CLOSED_STATE, null);
    }

    public static void closeReject(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(LeaveAppSMConstants.CLOSED_STATE, LeaveAppSMConstants.REJECTED);
    }

    public static void closeCancel(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(LeaveAppSMConstants.CLOSED_STATE, LeaveAppSMConstants.CANCELED);
    }

    public static void closeApprove(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(LeaveAppSMConstants.CLOSED_STATE, LeaveAppSMConstants.APPROVED);
    }

}
