package com.anshuman.statemachinedemo.workflow.config;


import com.anshuman.statemachinedemo.util.StringUtil;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;

/**
 * Instead of using a StateMachineListener interface, you can use a StateMachineInterceptor. One conceptual difference is that you can use an interceptor to
 * intercept and stop a current state change, or change its transition logic.
 *
 * @param <S> State Parameter class
 * @param <E> Event Parameter class
 */
@Slf4j
public class StateMachineInterceptor<S, E> extends StateMachineInterceptorAdapter<S, E> {

    @Override
    public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
        log.trace("preEvent: [stateMachine: {}, event to be passed: {}, hasError: {}, current state: {}]",
            stateMachine.getId(),
            message.getPayload(),
            stateMachine.hasStateMachineError(),
            stateMachine.getState().getId());
        return message;
    }

    @Override
    public void preStateChange(State<S, E> state, Message<E> message,
        Transition<S, E> transition, StateMachine<S, E> stateMachine,
        StateMachine<S, E> rootStateMachine) {
        log.trace("preStateChange: [stateMachine: {}, current state: {}, event to be passed: {}, transition: {name: {}, source state: {}, target state: {}}]",
            stateMachine.getId(),
            state.getId(),
            Optional.ofNullable(message).map(Message::getPayload).map(E::toString).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getName).filter(Predicate.not(String::isEmpty)).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getSource).map(State::getId).map(S::toString).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getTarget).map(State::getId).map(S::toString).orElse("null")
            );
    }

    @Override
    public void postStateChange(State<S, E> state, Message<E> message,
        Transition<S, E> transition, StateMachine<S, E> stateMachine,
        StateMachine<S, E> rootStateMachine) {
        log.trace("postStateChange: [stateMachine: {}, current state: {}, event passed: {}, transition: {name: {}, source state: {}, target state: {}}]",
            stateMachine.getId(),
            state.getId(),
            Optional.ofNullable(message).map(Message::getPayload).map(E::toString).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getName).filter(Predicate.not(String::isEmpty)).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getSource).map(State::getId).map(S::toString).orElse("null"),
            Optional.ofNullable(transition).map(Transition::getTarget).map(State::getId).map(S::toString).orElse("null")
        );
    }

    @Override
    public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {

        log.trace("preTransition: [stateMachine: {}, stage: {}, source state: {}, event: {}, target state: {}, extended state: [{}]]",
            stateContext.getStateMachine().getId(),
            StringUtil.stageFromContext(stateContext),
            StringUtil.sourceStateFromContext(stateContext),
            StringUtil.eventFromContext(stateContext),
            StringUtil.targetStateFromContext(stateContext),
            StringUtil.extendedStateFromContext(stateContext));
        return stateContext;
    }

    @Override
    public StateContext<S, E> postTransition(StateContext<S, E> stateContext) {
        log.trace("postTransition: [stateMachine: {}, stage: {}, source state: {}, event: {}, target state: {}, extended state: [{}]]",
            stateContext.getStateMachine().getId(),
            StringUtil.stageFromContext(stateContext),
            StringUtil.sourceStateFromContext(stateContext),
            StringUtil.eventFromContext(stateContext),
            StringUtil.targetStateFromContext(stateContext),
            StringUtil.extendedStateFromContext(stateContext));
        return stateContext;
    }

    @Override
    public Exception stateMachineError(StateMachine<S, E> stateMachine,
        Exception exception) {
        log.error("StateMachineError: [stateMachine: {}, current state: {}, hasError: {}, error message: {}]",
            stateMachine.getId(), stateMachine.getState().getId(), stateMachine.hasStateMachineError(), exception.getMessage());
        return exception;
    }
}
