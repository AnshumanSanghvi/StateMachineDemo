package com.anshuman.statemachinedemo.workflow.model;

import org.springframework.statemachine.StateMachineContext;

public interface ContextEntity<S, E> {

    StateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(StateMachineContext<S, E> context);

}