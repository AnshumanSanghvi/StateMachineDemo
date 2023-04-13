package com.sttl.hrms.workflow.statemachine.builder;

import com.sttl.hrms.workflow.data.enums.WorkFlowTypeStateMachine;
import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.statemachine.SMConstants;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import org.springframework.statemachine.StateMachine;

public class StateMachineBuilderFactory {

    private StateMachineBuilderFactory() {
    }

    public static StateMachine<String, String> getStateMachine(String stateMachineId) {
        try {
            return switch (stateMachineId) {
                case SMConstants.LEAVE_APP_WF_V1 -> StateMachineBuilder.createStateMachine(SMConstants.LEAVE_APP_WF_V1,
                        null, null, false, 3, 3);
                case SMConstants.LOAN_APP_WF_V1 -> StateMachineBuilder.createStateMachine(SMConstants.LOAN_APP_WF_V1,
                        null, null, false, 3, 3);
                default -> throw new StateMachineException("Could not find a statemachine builder " +
                        "associated with stateMachineId: " + stateMachineId);
            };
        } catch (Exception ex) {
            throw new StateMachineException(ex);
        }
    }

    public static StateMachine<String, String> getStateMachine(WorkflowType workflowType) {
        String stateMachineId = WorkFlowTypeStateMachine.getLatestSMIdFromWFType(workflowType);
        return getStateMachine(stateMachineId);
    }
}
