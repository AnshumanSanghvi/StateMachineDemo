package com.anshuman.statemachinedemo;

import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverApprovesApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRejectsApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRequestsChangesToApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRollsBackApprovalOfApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.approverRollsBackRejectionOfApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.closeApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.userCancelsApplication;
import static com.anshuman.statemachinedemo.LeaveApplicationWorkflowTest.LeaveApplicationWorkflowScenarios.userSubmitsApplication;
import static com.anshuman.statemachinedemo.workflows.WFHelper.invokeStateChanges;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anshuman.statemachinedemo.config.StateMachineConfig;
import com.anshuman.statemachinedemo.config.StateMachineConfig.AppEvent;
import com.anshuman.statemachinedemo.config.StateMachineConfig.AppState;
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
@ContextConfiguration(classes = StateMachineConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class LeaveApplicationWorkflowTest {

    @Autowired
    private StateMachine<AppState, AppEvent> stateMachine;

    @Test
    @Order(1)
    public void testUserSubmitsApplication() {
        invokeStateChanges((str, sm) -> userSubmitsApplication(stateMachine),
            "userSubmitsApplication", stateMachine);
        assertEquals(AppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverApprovesApplication() {
        invokeStateChanges((str, sm) -> approverApprovesApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverApprovesIt", stateMachine);
        assertEquals(AppState.APPROVED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRejectsApplication() {
        invokeStateChanges((str, sm) -> approverRejectsApplication(stateMachine),
            "userSubmitsApplicationAndThenApproverRejectsIt", stateMachine);
        assertEquals(AppState.REJECTED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testUserCancelsApplication() {
        invokeStateChanges((str, sm) -> userCancelsApplication(stateMachine),
            "userSubmitsApplicationAndThenUserCancelsIt", stateMachine);
        assertEquals(AppState.CANCELED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRequestsChangesToApplication() {
        invokeStateChanges((str, sm) -> approverRequestsChangesToApplication(stateMachine),
            "approverReviewsApplicationThenApproverRequestsChanges", stateMachine);
        assertEquals(AppState.CREATED, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRollsBackApproval() {
        invokeStateChanges((str, sm) -> approverRollsBackApprovalOfApplication(stateMachine),
            "approverApprovesApplicationAndThenApproverRollsBackApproval", stateMachine);
        assertEquals(AppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(1)
    public void testApproverRollsBackRejection() {
        invokeStateChanges((str, sm) -> approverRollsBackRejectionOfApplication(stateMachine),
            "approverRjectsApplicationAndThenApproverRollsBackRejection", stateMachine);
        assertEquals(AppState.UNDER_PROCESS, stateMachine.getState().getId());
    }

    @Test
    @Order(999)
    public void testCloseApplication() {
        invokeStateChanges((str, sm) -> closeApplication(stateMachine),
            "closeApplication", stateMachine);
        assertEquals(AppState.CLOSED, stateMachine.getState().getId());
    }

    static class LeaveApplicationWorkflowScenarios {
        public static void userSubmitsApplication(StateMachine<AppState, AppEvent> stateMachine) {
            if (stateMachine.getState().getId().equals(AppState.CREATED)) {
                stateMachine.sendEvent(AppEvent.SUBMIT);
                stateMachine.sendEvent(AppEvent.TRIGGER_REVIEW_OF);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverApprovesApplication(StateMachine<AppState, AppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(AppEvent.APPROVE);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRejectsApplication(StateMachine<AppState, AppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(AppEvent.REJECT);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void userCancelsApplication(StateMachine<AppState, AppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(AppEvent.CANCEL);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRequestsChangesToApplication(StateMachine<AppState, AppEvent> stateMachine) {
            userSubmitsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.UNDER_PROCESS)) {
                stateMachine.sendEvent(AppEvent.REQUEST_CHANGES_IN);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRollsBackApprovalOfApplication(StateMachine<AppState, AppEvent> stateMachine) {
            approverApprovesApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.APPROVED)) {
                stateMachine.sendEvent(AppEvent.ROLL_BACK_APPROVAL);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }
        }

        public static void approverRollsBackRejectionOfApplication(StateMachine<AppState, AppEvent> stateMachine) {
            approverRejectsApplication(stateMachine);
            if (stateMachine.getState().getId().equals(AppState.REJECTED)) {
                stateMachine.sendEvent(AppEvent.ROLL_BACK_REJECTION);
            } else {
                stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
            }

        }

        public static void closeApplication(StateMachine<AppState, AppEvent> stateMachine) {
            userCancelsApplication(stateMachine);
            AppState currentState = stateMachine.getState().getId();
            boolean isApproved = currentState.name().equalsIgnoreCase(AppState.APPROVED.name());
            boolean isRejected = currentState.name().equalsIgnoreCase(AppState.REJECTED.name());
            boolean isCanceled = currentState.name().equalsIgnoreCase(AppState.CANCELED.name());
            if (isApproved || isRejected || isCanceled) {
                stateMachine.sendEvent(AppEvent.TRIGGER_CLOSE);
            }
            else {
                stateMachine.setStateMachineError(new RuntimeException("Closing application from a non-terminal state."));
            }
        }
    }






}
