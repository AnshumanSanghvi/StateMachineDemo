package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.APPROVED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.CANCELED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.CLOSED_STATE;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.IS_PARALLEL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.ONLY_FORWARD_WITH_APPROVAL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.REJECTED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.RETURN_COUNT;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.ROLL_BACK_COUNT;

import java.util.Map;
import org.springframework.statemachine.ExtendedState;

public class LeaveAppStateMachineActions {

    public static void initiateLeaveAppWorkflow(ExtendedState extendedState) {
        Map<Object, Object> map = extendedState.getVariables();
        // Parallel: If any approver approves, then application is approved.
        // Serial: all approvers need to approve in order of hierarchy.
        map.put(IS_PARALLEL, false);

        // is this the first time the application is in this state, or has application been rolled back?
        map.put(ROLL_BACK_COUNT, 0);

        // is this the first the application is in this state, or has application been returned for changes?
        map.put(RETURN_COUNT, 0);

        // for future use: can an approver send the application forward in the hierarchy without approval?
        map.put(ONLY_FORWARD_WITH_APPROVAL, true);
    }

    public static void returnBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(RETURN_COUNT, 0) + 1;
        map.put(RETURN_COUNT, + newCount);
    }

    public static void rollBack(ExtendedState extendedState) {
        var map = extendedState.getVariables();
        int newCount = (Integer) map.getOrDefault(ROLL_BACK_COUNT, 0) + 1;
        map.put(ROLL_BACK_COUNT, + newCount);
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
