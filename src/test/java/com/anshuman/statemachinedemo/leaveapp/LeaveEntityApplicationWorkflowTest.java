package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.APPROVED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.CANCELED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.CLOSED_STATE;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.REJECTED;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.RETURN_COUNT;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.ROLL_BACK_COUNT;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverApprovesApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRejectsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRequestsChangesToApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRollsBackApprovalOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.approverRollsBackRejectionOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.userCancelsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowScenarios.userSubmitsApplication;
import static com.anshuman.statemachinedemo.workflows.WFHelper.invokeStateChanges;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LeaveAppStateMachineConfig.class)
public class LeaveEntityApplicationWorkflowTest {

    @Autowired
    private StateMachineFactory<LeaveAppState, LeaveAppEvent> stateMachineFactory;
    private StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

    @PostConstruct
    public void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    public void testUserSubmitsApplication() {
        invokeStateChanges((str, sm) -> userSubmitsApplication(stateMachine),
            "userSubmitsApplication", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    public void testApproverApprovesApplication() {
        invokeStateChanges((str, sm) -> approverApprovesApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverApprovesIt", stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(APPROVED));
    }

    @Test
    public void testApproverRejectsApplication() {
        invokeStateChanges((str, sm) -> approverRejectsApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverRejectsIt", stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(REJECTED));
    }

    @Test
    public void testUserCancelsApplication() {
        invokeStateChanges((str, sm) -> userCancelsApplication(stateMachine),
            "userSubmitsApplicationAndThenUserCancelsIt", stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
        assertTrue(((String) stateMachine.getExtendedState().getVariables().get(CLOSED_STATE)).equalsIgnoreCase(CANCELED));
    }

    @Test
    public void testApproverRequestsChangesToApplication() {
        invokeStateChanges((str, sm) -> approverRequestsChangesToApplication(stateMachine),
            "approverReviewsApplicationThenApproverRequestsChanges", stateMachine);
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());
        assertEquals(((Integer) stateMachine.getExtendedState().getVariables().get(RETURN_COUNT)), 1);
    }

    @Test
    public void testApproverRollsBackApproval() {
        invokeStateChanges((str, sm) -> approverRollsBackApprovalOfApplication(stateMachine),
            "approverApprovesApplicationAndThenApproverRollsBackApproval", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
        assertEquals(((Integer) stateMachine.getExtendedState().getVariables().get(ROLL_BACK_COUNT)), 1);
    }

    @Test
    public void testApproverRollsBackRejection() {
        invokeStateChanges((str, sm) -> approverRollsBackRejectionOfApplication(stateMachine),
            "approverRjectsApplicationAndThenApproverRollsBackRejection", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

}
