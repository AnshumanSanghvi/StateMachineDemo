package com.anshuman.statemachinedemo.workflow.util;

import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

@Slf4j
@UtilityClass
public class WFHelper {

    public static <S, E> void invokeStateChanges(Consumer<StateMachine<S, E>> stateChangesConsumer, StateMachine<S, E> stateMachine) {
        stateMachine.startReactively().block();
        log.debug("Initial State: {}", StringUtil.state(stateMachine.getState()));
        stateChangesConsumer.accept(stateMachine);
        log.debug("Final State: {}, isComplete? {}", StringUtil.state(stateMachine.getState()), stateMachine.isComplete());

        stateMachine.stopReactively().block();
    }

    public static <E> Message<E> toMessage(E event) {
        return MessageBuilder.withPayload(event).build();
    }

}
