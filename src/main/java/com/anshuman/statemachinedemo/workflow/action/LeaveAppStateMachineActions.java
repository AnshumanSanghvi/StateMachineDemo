package com.anshuman.statemachinedemo.workflow.action;

import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.CLOSED_STATE;
import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.IS_PARALLEL;
import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.ONLY_FORWARD_WITH_APPROVAL;
import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.RETURN_COUNT;
import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.ROLL_BACK_COUNT;

import com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants;
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
