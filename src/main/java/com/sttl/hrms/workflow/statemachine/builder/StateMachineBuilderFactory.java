package com.sttl.hrms.workflow.statemachine.builder;

import com.sttl.hrms.workflow.data.enums.WorkFlowTypeStateMachine;
import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import org.springframework.statemachine.StateMachine;

import static com.sttl.hrms.workflow.statemachine.SMConstants.LEAVE_APP_WF_V1;
import static com.sttl.hrms.workflow.statemachine.SMConstants.LOAN_APP_WF_V1;

public class StateMachineBuilderFactory {

    private StateMachineBuilderFactory() {
    }

    public static StateMachine<String, String> getStateMachine(String stateMachineId) {
        try {
            return switch (stateMachineId) {
                case LEAVE_APP_WF_V1 -> StateMachineBuilder.createStateMachine(LEAVE_APP_WF_V1, null, null, false, 3, 3);
                case LOAN_APP_WF_V1 -> StateMachineBuilder.createStateMachine(LOAN_APP_WF_V1, null, null, false, 3, 3);
                default -> throw new StateMachineException("Could not find a statemachine builder associated with stateMachineId: " + stateMachineId);
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
