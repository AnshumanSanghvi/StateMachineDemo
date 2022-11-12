package com.anshuman.statemachinedemo.leaveapp;

import org.springframework.statemachine.StateMachine;

public class LeaveApplicationWorkflowScenarios {

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
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            stateMachine.sendEvent(LeaveAppEvent.ROLL_BACK);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }
    }

    public static void approverRollsBackRejectionOfApplication(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        approverRejectsApplication(stateMachine);
        if (stateMachine.getState().getId().equals(LeaveAppState.CLOSED)) {
            stateMachine.sendEvent(LeaveAppEvent.ROLL_BACK);
        } else {
            stateMachine.setStateMachineError(new RuntimeException("Cannot process application, incorrect source state."));
        }

    }
}
