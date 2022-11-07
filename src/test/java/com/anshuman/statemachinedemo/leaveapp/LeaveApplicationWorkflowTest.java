package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverApprovesApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRejectsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRequestsChangesToApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRollsBackApprovalOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRollsBackRejectionOfApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.closeApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.userCancelsApplication;
import static com.anshuman.statemachinedemo.leaveapp.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.userSubmitsApplication;
import static com.anshuman.statemachinedemo.workflows.WFHelper.invokeStateChanges;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LeaveAppStateMachineConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class LeaveApplicationWorkflowTest {

    @Autowired
    private StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

    @Test
    @Order(1)
    public void testUserSubmitsApplication() {
        invokeStateChanges((str, sm) -> userSubmitsApplication(stateMachine),
            "userSubmitsApplication", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverApprovesApplication() {
        invokeStateChanges((str, sm) -> approverApprovesApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverApprovesIt", stateMachine);
        assertEquals(LeaveAppState.APPROVED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRejectsApplication() {
        invokeStateChanges((str, sm) -> approverRejectsApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverRejectsIt", stateMachine);
        assertEquals(LeaveAppState.REJECTED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testUserCancelsApplication() {
        invokeStateChanges((str, sm) -> userCancelsApplication(stateMachine),
            "userSubmitsApplicationAndThenUserCancelsIt", stateMachine);
        assertEquals(LeaveAppState.CANCELED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRequestsChangesToApplication() {
        invokeStateChanges((str, sm) -> approverRequestsChangesToApplication(stateMachine),
            "approverReviewsApplicationThenApproverRequestsChanges", stateMachine);
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRollsBackApproval() {
        invokeStateChanges((str, sm) -> approverRollsBackApprovalOfApplication(stateMachine),
            "approverApprovesApplicationAndThenApproverRollsBackApproval", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRollsBackRejection() {
        invokeStateChanges((str, sm) -> approverRollsBackRejectionOfApplication(stateMachine),
            "approverRjectsApplicationAndThenApproverRollsBackRejection", stateMachine);
        assertEquals(LeaveAppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(999)
    public void testCloseApplication() {
        invokeStateChanges((str, sm) -> closeApplication(stateMachine),
            "closeApplication", stateMachine);
        assertEquals(LeaveAppState.CLOSED, stateMachine.getState().getId());
    }

    static class LeaveApplicationWorkflowScenarios {
        public static void userSubmitsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            if (stateMachine.getState().getId().equals(LeaveAppState.CREATED)) {
                stateMachine.sendEvent(LeaveAppEvent.SUBMIT);
                stateMachine.sendEvent(LeaveAppEvent.TRIGGER_REVIEW_OF);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverApprovesApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(LeaveAppEvent.APPROVE);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRejectsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(LeaveAppEvent.REJECT);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void userCancelsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(LeaveAppEvent.CANCEL);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRequestsChangesToApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(LeaveAppEvent.REQUEST_CHANGES_IN);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRollsBackApprovalOfApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            approverApprovesApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.APPROVED)) {
                stateMachine.sendEvent(LeaveAppEvent.ROLL_BACK_APPROVAL);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRollsBackRejectionOfApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            approverRejectsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(LeaveAppState.REJECTED)) {
                stateMachine.sendEvent(LeaveAppEvent.ROLL_BACK_REJECTION);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }

        }

        public static void closeApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
            userCancelsApplication(stateMachine);
            LeaveAppState currentState = stateMachine.getState().getId();
            boolean isApproved = currentState.name().equalsIgnoreCase(LeaveAppState.APPROVED.name());
            boolean isRejected = currentState.name().equalsIgnoreCase(LeaveAppState.REJECTED.name());
            boolean isCanceled = currentState.name().equalsIgnoreCase(LeaveAppState.CANCELED.name());
            if (isApproved || isRejected || isCanceled) {
                stateMachine.sendEvent(LeaveAppEvent.TRIGGER_CLOSE);
            }
            else {
                stateMachine.setStateMachineError(new RuntimeException("Closing application from a non-terminal state."));
            }
        }
    }






}
