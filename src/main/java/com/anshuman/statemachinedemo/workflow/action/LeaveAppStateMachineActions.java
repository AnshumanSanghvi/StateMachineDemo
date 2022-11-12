package com.anshuman.statemachinedemo.workflow.action;

import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CLOSED_STATE;

import com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants;
import java.util.Map;
import org.springframework.statemachine.ExtendedState;

public class LeaveAppStateMachineActions {

    private LeaveAppStateMachineActions() {
        // use class statically
    }

    public static void initiateLeaveAppWorkflow(ExtendedState extendedState) {
        Map<Object, Object> map = extendedState.getVariables();
        // Parallel: If any approver approves, then application is approved.
        // Serial: all approvers need to approve in order of hierarchy.
        map.put(LeaveAppConstants.IS_PARALLEL, false);

        // is this the first time the application is in this state, or has application been rolled back?
        map.put(LeaveAppConstants.ROLL_BACK_COUNT, 0);

        // is this the first the application is in this state, or has application been returned for changes?
        map.put(LeaveAppConstants.RETURN_COUNT, 0);

        // for future use: can an approver send the application forward in the hierarchy without approval?
        map.put(LeaveAppConstants.ONLY_FORWARD_WITH_APPROVAL, true);
    }

    public static void returnBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(LeaveAppConstants.RETURN_COUNT, 0) + 1;
        map.put(LeaveAppConstants.RETURN_COUNT, newCount);
    }

    public static void rollBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(LeaveAppConstants.ROLL_BACK_COUNT, 0) + 1;
        map.put(LeaveAppConstants.ROLL_BACK_COUNT, newCount);
        map.put(CLOSED_STATE, null);
    }

    public static void closeReject(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, LeaveAppConstants.REJECTED);
    }

    public static void closeCancel(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, LeaveAppConstants.CANCELED);
    }

    public static void closeApprove(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, LeaveAppConstants.APPROVED);
    }

}
