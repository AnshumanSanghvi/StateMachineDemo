package com.anshuman.statemachinedemo.model.entity;

import org.springframework.statemachine.StateMachineContext;

public interface ContextEntity<S, E> {

    StateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(StateMachineContext<S, E> context);

}