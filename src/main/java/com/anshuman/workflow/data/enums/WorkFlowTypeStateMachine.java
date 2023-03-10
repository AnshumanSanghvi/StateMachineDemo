package com.anshuman.workflow.data.enums;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;
import static com.anshuman.workflow.statemachine.data.constant.LoanAppSMConstants.LOAN_APP_WF_V1;

import com.anshuman.workflow.statemachine.data.Pair;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public enum WorkFlowTypeStateMachine {
    LEAVE_APPLICATION_STATE_MACHINES(WorkflowType.LEAVE_APPLICATION, List.of(new Pair<>("v1", LEAVE_APP_WF_V1))),
    LOAN_APPLICATION_STATE_MACHINES(WorkflowType.LOAN_APPLICATION, List.of(new Pair<>("v1", LOAN_APP_WF_V1)));

    private final WorkflowType workflowType;
    private final List<Pair<String, String>> stateMachineIds;
    private static final WorkFlowTypeStateMachine[] values = WorkFlowTypeStateMachine.values();

    WorkFlowTypeStateMachine(WorkflowType workflowType, List<Pair<String, String>> stateMachineIds) {
        this.workflowType = workflowType;
        this.stateMachineIds = stateMachineIds;
    }

    public static List<Pair<String, String>> getStateMachineIds(WorkflowType workflowType) {
        for(WorkFlowTypeStateMachine wfsm : values) {
            if (wfsm.getWorkflowType().equals(workflowType))
                return wfsm.getStateMachineIds();
        }
        return Collections.emptyList();
    }
}
