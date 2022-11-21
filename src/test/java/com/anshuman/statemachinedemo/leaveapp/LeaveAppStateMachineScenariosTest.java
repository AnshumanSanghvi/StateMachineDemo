package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverApprovesApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRejectsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRequestsChangesToApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRollsBackApprovalOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRollsBackCanceledApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRollsBackRejectionOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.systemCompletesClosedApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.systemTriggersApplicationReviewProcess;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.userCancelsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.userInitializesApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.userSubmitsApplication;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.APPROVED;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CANCELED;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CLOSED_STATE;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.REJECTED;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.RETURN_COUNT;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.ROLL_BACK_COUNT;
import static com.anshuman.statemachinedemo.workflow.util.WFHelper.invokeStateChanges;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anshuman.statemachinedemo.workflow.config.LeaveAppWFStateMachineConfig;
import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LeaveAppWFStateMachineConfig.class)
class LeaveAppStateMachineScenariosTest {

    @Autowired
    private StateMachineFactory<LeaveAppState, LeaveAppEvent> stateMachineFactory;
    private StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

    @PostConstruct
    public void initialize() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    void testUserInitializesApplication() {
        invokeStateChanges((sm) -> userInitializesApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());

    }

    @Test
    void testUserSubmitsApplication() {
        invokeStateChanges((sm) -> userSubmitsApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.SUBMITTED, stateMachine.getState().getId());
    }

    @Test
    void testSystemTriggersApplicationReviewProcess() {
        invokeStateChanges((sm) -> systemTriggersApplicationReviewProcess(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    void testApproverApprovesApplication() {
        invokeStateChanges((sm) -> approverApprovesApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(APPROVED));
    }

    @Test
    void testApproverRejectsApplication() {
        invokeStateChanges((sm) -> approverRejectsApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(REJECTED));
    }

    @Test
    void testUserCancelsApplication() {
        invokeStateChanges((sm) -> userCancelsApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(CANCELED));
    }

    @Test
    void testApproverRequestsChangesToApplication() {
        invokeStateChanges((sm) -> approverRequestsChangesToApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());
        assertEquals(((Integer) stateMachine.getExtendedState().getVariables().get(RETURN_COUNT)), 1);
    }

    @Test
    void testApproverRollsBackApproval() {
        invokeStateChanges((sm) -> approverRollsBackApprovalOfApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
//        assertEquals(1, (Integer) stateMachine.getExtendedState().getVariables().get(ROLL_BACK_COUNT));
    }

    @Test
    void testApproverRollsBackRejection() {
        invokeStateChanges((sm) -> approverRollsBackRejectionOfApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
//        assertEquals(((Integer) stateMachine.getExtendedState().getVariables().get(ROLL_BACK_COUNT)), 1);
    }

    @Test
    void testSystemCompletesClosedApplication() {
        invokeStateChanges((sm) -> systemCompletesClosedApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.COMPLETED, stateMachine.getState().getId());
        assertTrue(stateMachine.isComplete());
    }

    @Test
    void testCannotRollbackCanceledApplication() {
        invokeStateChanges((sm) -> approverRollsBackCanceledApplication(stateMachine), stateMachine);
        hasError(stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertEquals(0, ((Integer) stateMachine.getExtendedState().getVariables().getOrDefault(ROLL_BACK_COUNT, 0)));
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(CANCELED));
    }

    private static void hasError(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        assertFalse(stateMachine.hasStateMachineError());
    }

}
