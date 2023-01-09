package com.anshuman.statemachinedemo.model.persist;

import com.anshuman.statemachinedemo.exception.StateMachineException;
import com.anshuman.statemachinedemo.exception.StateMachinePersistenceException;
import com.anshuman.statemachinedemo.util.StringUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a wrapper class over the DefaultStateMachinePersister.
 * We use its methods to persist or restore a state machine context from and to a state machine.
 *
 * @param <S> Parameter for the State class
 * @param <E> Parameter for the Event class
 * @param <T> Parameter for the context object class which provides the required StateMachineContext
 */
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DefaultStateMachineAdapter<S, E, T> {

    private final StateMachineFactory<S, E> stateMachineFactory;

    private final StateMachinePersister<S, E, T> stateMachinePersister;

    public StateMachine<S, E> restore(String stateMachineId, T contextObj) {
        try {
            StateMachine<S, E> stateMachine = stateMachinePersister.restore(create(stateMachineId), contextObj);
            log.debug("Restored stateMachine: {}", StringUtil.stateMachine(stateMachine, false));
            log.trace("Restored stateMachine with details: {}", StringUtil.stateMachine(stateMachine, true));
            return stateMachine;
        } catch (Exception e) {
            String errMsg = "Could not restore a state machine from the database, for a statemachine with id: " +
                stateMachineId + " and statemachine context: " + contextObj;
            throw new StateMachinePersistenceException(errMsg, e);
        }
    }

    public void persist(StateMachine<S, E> stateMachine, T contextObj) {
        try {
            stateMachinePersister.persist(stateMachine, contextObj);
            log.debug("Persisted stateMachine: {} for entity: {}", StringUtil.stateMachine(stateMachine, false), contextObj);
        } catch (Exception e) {
            String errMsg = "Could not save to the database the state machine context: " +
                contextObj + " for statemachine with id: " + stateMachine.getId();
            throw new StateMachinePersistenceException(errMsg, e);
        }
    }

    public StateMachine<S, E> create(String stateMachineId) {
        try {
            StateMachine<S, E> stateMachine = Optional
                .ofNullable(stateMachineId)
                .map(stateMachineFactory::getStateMachine) // create statemachine with machineId
                .orElse(stateMachineFactory.getStateMachine()); // create statemachine without machineId
            stateMachine.startReactively().block();
            log.debug("Created and started stateMachine: {}", StringUtil.stateMachine(stateMachine, false));
            return stateMachine;
        } catch (Exception e) {
            String errMsg = "Could not create a new state machine from the state machine factory with machineId: " + stateMachineId;
            throw new StateMachineException(errMsg, e);
        }
    }

}
