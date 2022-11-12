package com.anshuman.statemachinedemo.leaveapp;

import com.anshuman.statemachinedemo.workflows.ContextEntity;
import com.anshuman.statemachinedemo.workflows.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.workflows.enums.AdminActionScopeWF;
import com.anshuman.statemachinedemo.workflows.enums.ApproverFlowTypeForWF;
import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LeaveAppWFConfigurator {

    private final DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, ContextEntity<LeaveAppState, LeaveAppEvent, ? extends Serializable>> leaveAppStateMachineAdapter;


    public void createWorkflow(ApproverFlowTypeForWF approverFlowTypeForWF, Boolean allowAdminIntervention,
        AdminActionScopeWF adminActionScopeWF, int adminApproveRoleId, int maxNoOfChangeRequests, int numOfApprovers,
        List<Integer> approverRoleIds, Boolean allowRollBack, Boolean allowSameApprovers, Boolean sendEmailReminder,
        int reminderIntervalDays) {

    }

    public void updateWorkflow(ApproverFlowTypeForWF approverFlowTypeForWF, Boolean allowAdminIntervention,
        AdminActionScopeWF adminActionScopeWF, int adminApproveRoleId, int maxNoOfChangeRequests, int numOfApprovers,
        List<Integer> approverRoleIds, Boolean allowRollBack, Boolean allowSameApprovers, Boolean sendEmailReminder,
        int reminderIntervalDays) {

    }

    public void getWorkflow(long workflowInstanceId) throws Exception {


    }

}
