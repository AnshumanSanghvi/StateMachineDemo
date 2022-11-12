package com.anshuman.statemachinedemo.config;

import com.anshuman.statemachinedemo.workflows.ContextEntity;
import com.anshuman.statemachinedemo.workflows.DefaultStateMachineAdapter;
import java.io.Serializable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

@Configuration
public class StateMachinePersistenceConfig {

    @Bean
    public <S, E> StateMachinePersist<S, E, ContextEntity<S, E, Serializable>> persist() {
        return new StateMachinePersist<>() {
            @Override
            public void write(StateMachineContext<S, E> context, ContextEntity<S, E, Serializable> contextObj) throws Exception {
                contextObj.setStateMachineContext(context);
            }

            @Override
            public StateMachineContext<S, E> read(ContextEntity<S, E, Serializable> contextObj) throws Exception {
                return contextObj.getStateMachineContext();
            }
        };
    }

    @Bean
    public <S, E> StateMachinePersister<S, E, ContextEntity<S, E, Serializable>> persister(
        StateMachinePersist<S, E, ContextEntity<S, E, Serializable>> persist) {
        return new DefaultStateMachinePersister<>(persist);
    }

    @Bean
    public <S, E, T> DefaultStateMachineAdapter<S, E, T> stateMachineAdapter(StateMachineFactory<S, E> stateMachineFactory,
        StateMachinePersister<S, E, T> stateMachinePersister) {
        return new DefaultStateMachineAdapter<>(stateMachineFactory, stateMachinePersister);
    }

}
