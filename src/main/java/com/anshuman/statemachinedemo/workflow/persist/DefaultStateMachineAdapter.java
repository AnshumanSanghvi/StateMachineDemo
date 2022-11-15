package com.anshuman.statemachinedemo.workflow.persist;

import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * This is a wrapper class over the DefualtStateMachinePersister. We use its method to persist and restore a state machine context.
 * @param <S> Parameter for the State class
 * @param <E> Parameter for the Event class
 * @param <T> Paremeter for the context object class which provides the required StateMachineContext
 */
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
        stateMachine.startReactively();
        return stateMachine;
    }



}
