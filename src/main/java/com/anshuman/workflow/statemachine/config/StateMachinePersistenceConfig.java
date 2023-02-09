package com.anshuman.workflow.statemachine.config;

import com.anshuman.workflow.data.model.entity.ContextEntity;
import com.anshuman.workflow.statemachine.persist.DefaultStateMachineAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;

@Configuration
public class StateMachinePersistenceConfig {

    /**
     * You cannot persist a StateMachine by using normal java serialization,
     * as the object graph is too rich and contains too many dependencies on other Spring context classes.
     * StateMachineContext is a runtime representation of a state machine,
     * that you can use to restore an existing machine into a state represented by a particular StateMachineContext object.
     * @param <S> Parameter for StateAction class
     * @param <E> Parameter for TestEvent class
     * @return An instance of the StateMachinePersist interface, responsible for serialization and deserialization of a StateMachineContext
     */
    @Bean
    public <S, E> StateMachinePersist<S, E, ContextEntity<S, E>> stateMachinePersist() {
        return new StateMachinePersist<>() {
            @SuppressWarnings("RedundantThrows")
            @Override
            public void write(StateMachineContext<S, E> context, ContextEntity<S, E> contextObj) throws Exception {
                contextObj.setStateMachineContext(new DefaultStateMachineContext<>(context.getState(), context.getEvent(),
                    context.getEventHeaders(), context.getExtendedState()));
            }

            @SuppressWarnings("RedundantThrows")
            @Override
            public StateMachineContext<S, E> read(ContextEntity<S, E> contextObj) throws Exception {
                return contextObj.getStateMachineContext();
            }
        };
    }

    /**
     * @param <S> Parameter for the StateAction class
     * @param <E> Parameter for the TestEvent class
     * @return The DefaultStateMachinePersister which is an implementation of the StateMachinePersister interface, which is responsible for persisting and
     * restoring a state machine from a persistent storage.
     */
    @Bean
    public <S, E> StateMachinePersister<S, E, ContextEntity<S, E>> stateMachinePersister(
        @Autowired StateMachinePersist<S, E, ContextEntity<S, E>> stateMachinePersist) {
        return new DefaultStateMachinePersister<>(stateMachinePersist);
    }

    /**
     * @param stateMachineFactory The StateMachineFactory bean
     * @param <S>                 Parameter for the StateAction class
     * @param <E>                 Parameter for the TestEvent class
     * @return A bean of the DefaultStateMachineAdapter
     */
    @Bean
    public <S, E> DefaultStateMachineAdapter<S, E, ContextEntity<S, E>> stateMachineAdapter(
        @Autowired StateMachineFactory<S, E> stateMachineFactory,
        @Autowired StateMachinePersister<S, E, ContextEntity<S, E>> stateMachinePersister) {
        return new DefaultStateMachineAdapter<>(stateMachineFactory, stateMachinePersister);
    }

}
