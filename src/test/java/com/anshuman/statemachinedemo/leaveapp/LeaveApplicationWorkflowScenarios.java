package com.anshuman.statemachinedemo.leaveapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public class LeaveApplicationWorkflowScenarios {

    public static void userInitializedApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        if (stateMachine.getState().getId().equals(LeaveAppState.INITIAL)) {
            sendEvent(stateMachine, LeaveAppEvent.START);
        }
        else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void userSubmitsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        userInitializedApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CREATED)) {
            sendEvent(stateMachine, LeaveAppEvent.SUBMIT);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void systemTriggersApplicationReviewProcess(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        userSubmitsApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.SUBMITTED)) {
            sendEvent(stateMachine, LeaveAppEvent.TRIGGER_REVIEW_OF);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }

    }

    public static void approverApprovesApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        systemTriggersApplicationReviewProcess(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
            sendEvent(stateMachine, LeaveAppEvent.APPROVE);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void approverRejectsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        systemTriggersApplicationReviewProcess(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
            sendEvent(stateMachine, LeaveAppEvent.REJECT);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void userCancelsApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        systemTriggersApplicationReviewProcess(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
            sendEvent(stateMachine, LeaveAppEvent.CANCEL);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void approverRequestsChangesToApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        systemTriggersApplicationReviewProcess(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.UNDER_PROCESS)) {
            sendEvent(stateMachine, LeaveAppEvent.REQUEST_CHANGES_IN);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }


    }

    public static void approverRollsBackApprovalOfApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        approverApprovesApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            sendEvent(stateMachine, LeaveAppEvent.ROLL_BACK);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void approverRollsBackRejectionOfApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        approverRejectsApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            sendEvent(stateMachine, LeaveAppEvent.ROLL_BACK);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void systemCompletesClosedApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        approverRejectsApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            sendEvent(stateMachine, LeaveAppEvent.TRIGGER_COMPLETE);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void approverRollsBackCanceledApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        userCancelsApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            sendEvent(stateMachine, LeaveAppEvent.ROLL_BACK);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    private static void sendEvent(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine, LeaveAppEvent event) {
        boolean eventSent = stateMachine.sendEvent(event);
        if (!eventSent)
            log.warn("Event: {} was not sent to the stateMachine with id: {}, and having state: {}", event, stateMachine.getId(),
                stateMachine.getState().getId());
    }
}
