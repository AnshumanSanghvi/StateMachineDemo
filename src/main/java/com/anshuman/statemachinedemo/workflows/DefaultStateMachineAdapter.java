package com.anshuman.statemachinedemo.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

@RequiredArgsConstructor
public class DefaultStateMachineAdapter<S, E, T> {

    private final StateMachineFactory<S, E> stateMachineFactory;

    private final StateMachinePersister<S, E, T> persister;

    public StateMachine<S, E> restore(T contextObj) throws Exception {
        StateMachine<S, E> stateMachine = stateMachineFactory.getStateMachine();
        return persister.restore(stateMachine, contextObj);
    }

    public void persist(StateMachine<S, E> stateMachine, T contextObj) throws Exception {
        persister.persist(stateMachine, contextObj);
    }

    public StateMachine<S, E> create() {
        StateMachine<S, E> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        return stateMachine;
    }



}
