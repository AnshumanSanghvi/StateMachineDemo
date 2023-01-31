package com.anshuman.workflow.statemachine.guard;


import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.CANCELED;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.CLOSED_STATE;

import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
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
