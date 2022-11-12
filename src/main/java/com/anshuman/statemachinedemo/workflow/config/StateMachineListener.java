package com.anshuman.statemachinedemo.workflow.config;

import com.anshuman.statemachinedemo.workflow.exception.StateMachineException;
import com.anshuman.statemachinedemo.workflow.util.StringUtil;
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
        log.debug("Transitioning: {}", StringUtil.transition(transition));
    }

    @Override
    public void stateContext(StateContext<S, E> stateContext) {
        log.trace("State Context: {} {} {} {} {}",
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
        log.trace("Event not accepted. Event Headers: {}", StringUtil.messageHeaders(event.getHeaders()));
        log.error("Event not accepted. Event Payload: {}", event.getPayload());
    }

    @Override
    public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
        log.error("Exception encountered on stateMachine {} with uuid: {}", stateMachine.getId(), stateMachine.getUuid(), exception);

        log.trace("StateMachine Exception additional info: id: {}, uuid: {}, hasException: {}, current state: {}, extendedState: {}",
            stateMachine.getId(), stateMachine.getUuid(), stateMachine.hasStateMachineError(), stateMachine.getState().getId(),
            StringUtil.extendedState(stateMachine.getExtendedState()));

        throw new StateMachineException(exception);
    }

}
