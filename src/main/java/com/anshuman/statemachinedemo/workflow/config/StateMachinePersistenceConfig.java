package com.anshuman.statemachinedemo.workflow.config;

import com.anshuman.statemachinedemo.workflow.persist.ContextEntity;
import com.anshuman.statemachinedemo.workflow.persist.DefaultStateMachineAdapter;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

@Configuration
public class StateMachinePersistenceConfig {

    /**
     * You cannot persist a StateMachine by using normal java serialization,
     * as the object graph is too rich and contains too many dependencies on other Spring context classes.
     * StateMachineContext is a runtime representation of a state machine,
     * that you can use to restore an existing machine into a state represented by a particular StateMachineContext object.
     * @param <S> Parameter for State class
     * @param <E> Parameter for Event class
     * @param <I> Parameter for Identity class
     * @return An instance of the StateMachinePersist interface, responsible for serialization and deserialization of a StateMachineContext
     */
    @Bean
    public <S, E, I extends Serializable> StateMachinePersist<S, E, ContextEntity<S, E, I>> stateMachinePersist() {
        return new StateMachinePersist<>() {
            @Override
            public void write(StateMachineContext<S, E> context, ContextEntity<S, E, I> contextObj) throws Exception {
                contextObj.setStateMachineContext(context);
            }

            @Override
            public StateMachineContext<S, E> read(ContextEntity<S, E, I> contextObj) throws Exception {
                return contextObj.getStateMachineContext();
            }
        };
    }

    /**
     * @param <S> Parameter for the State class
     * @param <E> Parameter for the Event class
     * @param <I> Parameter for the Identity class
     * @return The DefaultStateMachinePersister which is an implementation of the StateMachinePersister interface, which is responsible for persisting and
     * restoring a state machine from a persistent storage.
     */
    @Bean
    public <S, E, I extends Serializable> StateMachinePersister<S, E, ContextEntity<S, E, I>> stateMachinePersister(
        @Autowired StateMachinePersist<S, E, ContextEntity<S, E, I>> stateMachinePersist) {
        return new DefaultStateMachinePersister<>(stateMachinePersist);
    }

    /**
     * @param stateMachineFactory The StateMachineFactory bean
     * @param <S>                 Parameter for the State class
     * @param <E>                 Parameter for the Event class
     * @param <I>                 Parameter for the Identity class
     * @return A bean of the DefaultStateMachineAdapter
     */
    @Bean
    public <S, E, I extends Serializable> DefaultStateMachineAdapter<S, E, ContextEntity<S, E, I>> stateMachineAdapter(
        @Autowired StateMachineFactory<S, E> stateMachineFactory,
        @Autowired StateMachinePersister<S, E, ContextEntity<S, E, I>> stateMachinePersister) {
        return new DefaultStateMachineAdapter<>(stateMachineFactory, stateMachinePersister);
    }

}
