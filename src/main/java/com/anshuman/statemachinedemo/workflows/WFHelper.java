package com.anshuman.statemachinedemo.workflows;

import com.anshuman.statemachinedemo.util.StringUtil;
import java.util.function.BiConsumer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
@UtilityClass
public class WFHelper {

    public static <S, E> void invokeStateChanges(BiConsumer<String, StateMachine<S, E>> stateChangesConsumer,
        String metaData, StateMachine<S, E> stateMachine) {
        log.debug("\n{}" + metaData);
        stateMachine.start();
        log.debug("Initial State: {}", StringUtil.state(stateMachine.getState()));
        stateChangesConsumer.accept(metaData, stateMachine);
        log.debug("Final State: {}, isComplete? {}", StringUtil.state(stateMachine.getState()), stateMachine.isComplete());
        stateMachine.stop();
    }

}
