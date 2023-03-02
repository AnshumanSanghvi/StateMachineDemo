package com.anshuman.workflow.statemachine.config;

import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Mono;

@Slf4j
public class StateMachineMonitor<S, E> extends AbstractStateMachineMonitor<S, E> {

    @Override
    public void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
        log.trace("TransitionActions: {} on stateMachine: {} took {} ms", transition.getName(), stateMachine.getId(), duration);
    }

    @Override
    public void action(StateMachine<S, E> stateMachine, Function<StateContext<S, E>, Mono<Void>> action,
        long duration) {
        log.trace("Action on stateMachine: {} took {} ms", stateMachine.getId(), duration);
    }
}