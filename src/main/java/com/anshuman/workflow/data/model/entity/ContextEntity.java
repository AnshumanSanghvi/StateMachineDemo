package com.anshuman.workflow.data.model.entity;

import org.springframework.statemachine.support.DefaultStateMachineContext;


public interface ContextEntity<S, E> {

    DefaultStateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(DefaultStateMachineContext<S, E> context);

}