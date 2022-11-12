package com.anshuman.statemachinedemo.workflows;

import com.anshuman.statemachinedemo.entity.Identifiable;
import java.io.Serializable;
import org.springframework.statemachine.StateMachineContext;

public interface ContextEntity<S, E, I extends Serializable> extends Identifiable<I> {

    StateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(StateMachineContext<S, E> context);

}