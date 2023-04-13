package com.sttl.hrms.workflow.statemachine.config;

import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class StateMachineObserver {

    @Slf4j
    public static class StateMachineListener extends StateMachineListenerAdapter<String, String> {

        @Override
        public void transition(Transition<String, String> transition) {
            log.trace("Transitioning: [{}]", StringUtil.transition(transition));
        }

        @Override
        public void stateContext(StateContext<String, String> stateContext) {
            log.trace("StateActions Context: [stateMachine: {}, stage: {}, source state: {}, event: {}, target state: {}, extended state: [{}]]",
                    stateContext.getStateMachine().getUuid() + " (" + stateContext.getStateMachine().getId() + ")",
                    StringUtil.stageFromContext(stateContext),
                    StringUtil.sourceStateFromContext(stateContext),
                    StringUtil.eventFromContext(stateContext),
                    StringUtil.targetStateFromContext(stateContext),
                    StringUtil.extendedStateFromContext(stateContext));
        }

        @Override
        public void eventNotAccepted(Message<String> event) {
            if (event == null) {
                log.error("Event not accepted, as the event message is null.");
                return;
            }
            log.error("Event not accepted. Name: {}", event.getPayload());

        }

        @Override
        public void stateMachineError(StateMachine<String, String> stateMachine, Exception exception) {
            log.error("Exception encountered on stateMachine {}", StringUtil.stateMachine(stateMachine, false), exception);
            log.trace("StateMachine Exception additional info: {}", StringUtil.stateMachine(stateMachine, true));
            throw new StateMachineException(exception);
        }

    }


    @Slf4j
    public static class StateMachineInterceptor extends StateMachineInterceptorAdapter<String, String> {

        @Override
        public Message<String> preEvent(Message<String> message, StateMachine<String, String> stateMachine) {
            logMessageHeaders(message);
            log.trace("preEvent: [stateMachine: {}, event to be passed: {}, hasError: {}, current state: {}]",
                    stateMachine.getId(),
                    message.getPayload(),
                    stateMachine.hasStateMachineError(),
                    stateMachine.getState().getId());
            return message;
        }

        @Override
        public void preStateChange(State<String, String> state, Message<String> message, Transition<String, String> transition,
                                   StateMachine<String, String> stateMachine, StateMachine<String, String> rootStateMachine) {
            logMessageHeaders(message);
            log.trace("preStateChange: [stateMachine: {}, current state: {}, event to be passed: {}, transition: {name: {}, source state: {}, target state: {}}]",
                    stateMachine.getId(),
                    state.getId(),
                    Optional.ofNullable(message).map(Message::getPayload).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getName).filter(Predicate.not(String::isEmpty)).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getSource).map(State::getId).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getTarget).map(State::getId).orElse("null")
            );
        }

        @Override
        public void postStateChange(State<String, String> state, Message<String> message, Transition<String, String> transition,
                                    StateMachine<String, String> stateMachine, StateMachine<String, String> rootStateMachine) {
            logMessageHeaders(message);
            log.trace("postStateChange: [stateMachine: {}, current state: {}, event passed: {}, transition: {name: {}, source state: {}, target state: {}}]",
                    stateMachine.getId(),
                    state.getId(),
                    Optional.ofNullable(message).map(Message::getPayload).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getName).filter(Predicate.not(String::isEmpty)).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getSource).map(State::getId).orElse("null"),
                    Optional.ofNullable(transition).map(Transition::getTarget).map(State::getId).orElse("null")
            );
        }

        @Override
        public StateContext<String, String> preTransition(StateContext<String, String> stateContext) {
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
        public StateContext<String, String> postTransition(StateContext<String, String> stateContext) {
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
        public Exception stateMachineError(StateMachine<String, String> stateMachine, Exception exception) {
            log.error("StateMachineError: [stateMachine: {}, current state: {}, hasError: {}, error message: {}]",
                    stateMachine.getId(), stateMachine.getState().getId(), stateMachine.hasStateMachineError(), exception.getMessage());
            return exception;
        }

        private void logMessageHeaders(Message<String> message) {
            String headers = StringUtil.messageHeaders(message);
            if (!headers.isBlank())
                log.debug("{}", StringUtil.messageHeaders(message));
        }
    }


    @Slf4j
    public static class StateMachineMonitor extends AbstractStateMachineMonitor<String, String> {
        @Override
        public void transition(StateMachine<String, String> stateMachine, Transition<String, String> transition, long duration) {
            if (duration / 1000 > 5)
                log.debug("Actions: {} on stateMachine: {} took {} ms", transition.getName(), stateMachine.getId(), duration);
            else
                log.trace("Actions: {} on stateMachine: {} took {} ms", transition.getName(), stateMachine.getId(), duration);
        }

        @Override
        public void action(StateMachine<String, String> stateMachine, Function<StateContext<String, String>, Mono<Void>> action, long duration) {
            if (duration / 1000 > 5)
                log.debug("Action on stateMachine: {} took {} ms", stateMachine.getId(), duration);
            else
                log.trace("Action on stateMachine: {} took {} ms", stateMachine.getId(), duration);
        }
    }

}
