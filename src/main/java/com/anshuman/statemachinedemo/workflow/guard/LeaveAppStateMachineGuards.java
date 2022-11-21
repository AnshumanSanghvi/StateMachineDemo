package com.anshuman.statemachinedemo.workflow.guard;


import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CANCELED;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CLOSED_STATE;

import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

@Slf4j
public class LeaveAppStateMachineGuards {

    public static boolean cannotRollBackCanceledApplication(StateContext<LeaveAppState, LeaveAppEvent> context) {
        String closedStateReason = (String) context.getExtendedState().getVariables().get(CLOSED_STATE);
        if (closedStateReason != null && closedStateReason.equalsIgnoreCase(CANCELED)) {
            log.warn("Cannot roll back the leave application that is canceled by the user");
            return false;
        }
        return true;
    }

}
