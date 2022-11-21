package com.anshuman.statemachinedemo.other.nonbean;


import com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

@Slf4j
public class MyActions {

    public static void initializeAction(StateContext<State, Event> context) {
        log.trace("Executing action: initializeAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        map.put(LeaveAppConstants.ROLL_BACK_COUNT, 0);
        map.put(LeaveAppConstants.RETURN_COUNT, 0);
        map.put(LeaveAppConstants.CLOSED_STATE, "");
    }

    public static void requestChangesAction(StateContext<State, Event> context) {
        log.trace("Executing action: requestChangesAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        int returnCount = (Integer) map.getOrDefault(LeaveAppConstants.RETURN_COUNT, 0);
        map.put(LeaveAppConstants.RETURN_COUNT, returnCount + 1);
    }

    public static void cancelAction(StateContext<State, Event> context) {
        log.trace("Executing action: cancelAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        map.put(LeaveAppConstants.CLOSED_STATE, "CANCELED");
    }

    public static void approveAction(StateContext<State, Event> context) {
        log.trace("Executing action: approveAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        map.put(LeaveAppConstants.CLOSED_STATE, "APPROVED");
    }

    public static void rejectAction(StateContext<State, Event> context) {
        log.trace("Executing action: rejectAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        map.put(LeaveAppConstants.CLOSED_STATE, "REJECTED");
    }

    public static void rollBackAction(StateContext<State, Event> context) {
        log.trace("Executing action: rollBackAction with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        int rollbackCount = (Integer) map.getOrDefault(LeaveAppConstants.ROLL_BACK_COUNT, 0);
        map.put(LeaveAppConstants.ROLL_BACK_COUNT, rollbackCount + 1);
    }

}
