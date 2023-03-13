package com.sttl.hrms.workflow.statemachine.persist;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.exception.StateMachinePersistenceException;
import com.sttl.hrms.workflow.statemachine.util.StringUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a wrapper class over the DefaultStateMachinePersister. We use its methods to persist or restore a state machine context from and to a state machine.
 *
 * @param <S> Parameter for the StateActions class
 * @param <E> Parameter for the Event class
 * @param <T> Parameter for the context object class which provides the required StateMachineContext
 */
@Slf4j
@Transactional
public class DefaultStateMachineAdapter<S, E, T> {

    @Qualifier("LeaveAppStateMachineFactory")
    private final StateMachineFactory<S, E> leaveAppStateMachineFactory;

    @Qualifier("LoanAppStateMachineFactory")
    private final StateMachineFactory<S, E> loanAppStateMachineFactory;

    private final StateMachinePersister<S, E, T> stateMachinePersister;

    public DefaultStateMachineAdapter(StateMachineFactory<S, E> leaveAppStateMachineFactory, StateMachineFactory<S, E> loanAppStateMachineFactory,
        StateMachinePersister<S, E, T> stateMachinePersister) {
        this.leaveAppStateMachineFactory = leaveAppStateMachineFactory;
        this.loanAppStateMachineFactory = loanAppStateMachineFactory;
        this.stateMachinePersister = stateMachinePersister;
    }

    public StateMachine<S, E> restore(StateMachine<S, E> stateMachine, T contextObj) {
        try {

            StateMachine<S, E> restoredStateMachine = stateMachinePersister.restore(stateMachine, contextObj);
            log.debug("Restored stateMachine: {}", StringUtil.stateMachine(restoredStateMachine, false));
            log.trace("Restored stateMachine with details: {}", StringUtil.stateMachine(restoredStateMachine, true));
            restoredStateMachine.startReactively().block();
            return restoredStateMachine;
        } catch (Exception e) {
            String errMsg = "Could not restore a state machine context from the database, for a statemachine with id: " +
                stateMachine.getId() + " and statemachine context: " + contextObj;
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

    public StateMachine<S, E> createStateMachine(String stateMachineId, WorkflowType workflowType) {
        try {
            StateMachine<S, E> stateMachine =  switch (workflowType) {
                case LEAVE_APPLICATION -> createStateMachineFromStateMachineFactory(stateMachineId, leaveAppStateMachineFactory);
                case LOAN_APPLICATION -> createStateMachineFromStateMachineFactory(stateMachineId, loanAppStateMachineFactory);
            };
            stateMachine.startReactively().block();
            log.debug("Created and started stateMachine: {}", StringUtil.stateMachine(stateMachine, false));
            return stateMachine;
        } catch (Exception e) {
            String errMsg = "Could not create a new state machine from the state machine factory with machineId: " + stateMachineId;
            throw new StateMachineException(errMsg, e);
        }
    }

    private StateMachine<S, E> createStateMachineFromStateMachineFactory(String stateMachineId, StateMachineFactory<S, E> stateMachineFactory) {
        return Optional
            .ofNullable(stateMachineId)
            .map(stateMachineFactory::getStateMachine) // create statemachine with machineId
            .orElse(stateMachineFactory.getStateMachine()); // create statemachine without machineId
    }

}
