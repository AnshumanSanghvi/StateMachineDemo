package com.anshuman.workflow.statemachine.config;

import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;

@Slf4j
public class StateMachineListener<S, E> extends StateMachineListenerAdapter<S, E> {

    @Override
    public void transition(Transition<S, E> transition) {
        log.trace("Transitioning: [{}]", StringUtil.transition(transition));
    }

    @Override
    public void stateContext(StateContext<S, E> stateContext) {
        log.trace("StateActions Context: [stateMachine: {}, stage: {}, source state: {}, event: {}, target state: {}, extended state: [{}]]",
            stateContext.getStateMachine().getUuid() + " (" + stateContext.getStateMachine().getId() + ")",
            StringUtil.stageFromContext(stateContext),
            StringUtil.sourceStateFromContext(stateContext),
            StringUtil.eventFromContext(stateContext),
            StringUtil.targetStateFromContext(stateContext),
            StringUtil.extendedStateFromContext(stateContext));
    }

    @Override
    public void eventNotAccepted(Message<E> event) {
        if (event == null) {
            log.error("Event not accepted, as the event message is null.");
            return;
        }
        log.error("Event not accepted. Name: {}", event.getPayload());

    }

    @Override
    public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
        log.error("Exception encountered on stateMachine {}", StringUtil.stateMachine(stateMachine, false), exception);
        log.trace("StateMachine Exception additional info: {}", StringUtil.stateMachine(stateMachine, true));
        throw new StateMachineException(exception);
    }

}
