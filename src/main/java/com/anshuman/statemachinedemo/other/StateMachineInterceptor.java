package com.anshuman.statemachinedemo.other;

import com.anshuman.statemachinedemo.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;

/**
 * Instead of using a StateMachineListener interface, you can use a StateMachineInterceptor.
 * One conceptual difference is that you can use an interceptor to intercept and stop a current state change,
 * or change its transition logic.
 * @param <S> State Parameter class
 * @param <E> Event Parameter class
 *
 *  stateMachine.getStateMachineAccessor().withRegion().addStateMachineInterception(new StateMachineInterceptor());
 */
@Slf4j
public class StateMachineInterceptor<S, E> extends StateMachineInterceptorAdapter<S, E> {

    @Override
    public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
        log.trace("preEvent- event to be passed: {}, to stateMachine  with id: {}  and current state: {}",
            message.getPayload(), stateMachine.getId(), stateMachine.getState().getId());
        return message;
    }

    @Override
    public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {
        log.trace("preTransition- State Context: {} {} {} {} {}",
            StringUtil.stageFromContext(stateContext),
            StringUtil.sourceStateFromContext(stateContext),
            StringUtil.eventFromContext(stateContext),
            StringUtil.targetStateFromContext(stateContext),
            StringUtil.extendedStateFromContext(stateContext));
        return stateContext;
    }

    @Override
    public void preStateChange(State<S, E> state, Message<E> message,
        Transition<S, E> transition, StateMachine<S, E> stateMachine,
        StateMachine<S, E> rootStateMachine) {
        log.trace("preStateChange- state {}, event: {}, transition: [{}], stateMachine: {}, rootStateMachine: {}",
            state.getId(), message.getPayload(), StringUtil.transition(transition), stateMachine.getId(), rootStateMachine.getId());
    }

    @Override
    public StateContext<S, E> postTransition(StateContext<S, E> stateContext) {
        log.trace("postTransition- State Context: {} {} {} {} {}",
            StringUtil.stageFromContext(stateContext),
            StringUtil.sourceStateFromContext(stateContext),
            StringUtil.eventFromContext(stateContext),
            StringUtil.targetStateFromContext(stateContext),
            StringUtil.extendedStateFromContext(stateContext));
        return stateContext;
    }

    @Override
    public void postStateChange(State<S, E> state, Message<E> message,
        Transition<S, E> transition, StateMachine<S, E> stateMachine,
        StateMachine<S, E> rootStateMachine) {
        log.trace("postStateChange- state {}, event: {}, transition: [{}], stateMachine: {}, rootStateMachine: {}",
            state.getId(), message.getPayload(), StringUtil.transition(transition), stateMachine.getId(), rootStateMachine.getId());
    }

    @Override
    public Exception stateMachineError(StateMachine<S, E> stateMachine,
        Exception exception) {
        log.error("StateMachineError- stateMachine id: {}, current state: {}, hasStateMachineError: {}, error message: {}",
            stateMachine.getId(), stateMachine.getState().getId(), stateMachine.hasStateMachineError(), exception.getMessage());
        return exception;
    }
}
