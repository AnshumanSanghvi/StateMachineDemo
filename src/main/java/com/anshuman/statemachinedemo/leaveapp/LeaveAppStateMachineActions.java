package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.APPROVED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.CANCELED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.CLOSED_STATE;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.IS_PARALLEL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.ONLY_FORWARD_WITH_APPROVAL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.REJECTED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.RETURN_COUNT;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppConst.ROLL_BACK_COUNT;

import java.util.Map;
import org.springframework.statemachine.ExtendedState;

public class LeaveAppStateMachineActions {

    public static void initiateLeaveAppWorkflow(ExtendedState extendedState) {
        Map<Object, Object> map = extendedState.getVariables();
        // Parallel: If any approver approves, then application is approved.
        // Serial: all approvers need to approve in order of hierarchy.
        map.put(IS_PARALLEL, false);

        // for future use: can an approver send the application forward in the hierarchy without approval?
        map.put(ONLY_FORWARD_WITH_APPROVAL, true);
    }

    public static void returnBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(RETURN_COUNT, 0) + 1;
        map.put(RETURN_COUNT, newCount);
    }

    public static void rollBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(ROLL_BACK_COUNT, 0) + 1;
        map.put(ROLL_BACK_COUNT, newCount);
        map.put(CLOSED_STATE, null);
    }

    public static void closeReject(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, REJECTED);
    }

    public static void closeCancel(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, CANCELED);
    }

    public static void closeApprove(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        map.put(CLOSED_STATE, APPROVED);
    }

}
