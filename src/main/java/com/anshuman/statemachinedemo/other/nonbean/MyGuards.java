package com.anshuman.statemachinedemo.other.nonbean;

import com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants;
import com.anshuman.statemachinedemo.workflow.exception.StateMachineException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

@Slf4j
public class MyGuards {

    public static boolean applicationReturnGuard(StateContext<State, Event> context) {
        log.trace("Calling guard: applicationReturnGuard with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        int returnCount = (Integer) map.getOrDefault(LeaveAppConstants.RETURN_COUNT, 0);
        log.trace("returnCount: {}", returnCount);
        boolean returnCountThreshold = returnCount <= 1;
        if (!returnCountThreshold) {
            context.getStateMachine().setStateMachineError(new StateMachineException("StateMachine exceeded return count threshold"));
        }
        return returnCountThreshold;
    }

    public static boolean applicationRollBackGuard(StateContext<State, Event> context) {
        log.trace("Calling guard: applicationRollBackGuard with currentState: {}", context.getStateMachine().getState().getId());
        Map<Object, Object> map = context.getExtendedState().getVariables();
        int rollbackCount = (Integer) map.getOrDefault(LeaveAppConstants.ROLL_BACK_COUNT, 0);
        log.trace("rollbackCount: {}", rollbackCount);
        boolean rollbackCountThreshold = rollbackCount <= 1;
        if (!rollbackCountThreshold) {
            context.getStateMachine().setStateMachineError(new StateMachineException("StateMachine exceeded roll back count threshold"));
        }
        return rollbackCountThreshold;
    }
}
