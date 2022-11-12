package com.anshuman.statemachinedemo.workflows;

import org.springframework.statemachine.StateMachine;

public interface IWorkflow<S, E> {

    StateMachine<S, E> loadStateMachine(long workflowInstanceId);

    S getCurrentState(Object workflow, long workflowInstanceId);

    boolean sendEventToStateMachine(StateMachine<S, E> stateMachine, long workflowInstanceId, E event);
}
