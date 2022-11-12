package com.anshuman.statemachinedemo;

import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.IS_PARALLEL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.ONLY_FORWARD_WITH_APPROVAL;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.RETURN_COUNT;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateExtended.ROLL_BACK_COUNT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.anshuman.statemachinedemo.config.StateMachinePersistenceConfig;
import com.anshuman.statemachinedemo.leaveapp.LeaveAppEvent;
import com.anshuman.statemachinedemo.leaveapp.LeaveAppRepository;
import com.anshuman.statemachinedemo.leaveapp.LeaveAppState;
import com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineConfig;
import com.anshuman.statemachinedemo.leaveapp.LeaveEntity;
import com.anshuman.statemachinedemo.workflows.ContextEntity;
import com.anshuman.statemachinedemo.workflows.DefaultStateMachineAdapter;
import java.util.Collections;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = {LeaveAppStateMachineConfig.class, StateMachinePersistenceConfig.class})
@EnableJpaRepositories(basePackageClasses = LeaveAppRepository.class)
@EntityScan(basePackageClasses = LeaveAppEvent.class)
public class StateMachineContextConverterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LeaveAppRepository repository;

    @Autowired
    private StateMachineFactory<LeaveAppState, LeaveAppEvent> stateMachineFactory;

    private StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

    @Autowired
    private DefaultStateMachinePersister<LeaveAppState, LeaveAppEvent, ContextEntity<LeaveAppState, LeaveAppEvent, Long>> persister;

    @PostConstruct
    private void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    void method(DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, LeaveEntity> stateMachineAdapter) throws Exception {
        // given
        LeaveEntity leaveEntity = new LeaveEntity();
        leaveEntity.setId(2L);
        stateMachineAdapter.persist(stateMachineAdapter.create(), leaveEntity);
        leaveEntity = repository.saveAndFlush(leaveEntity);
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        stateMachine.sendEvent(LeaveAppEvent.START);

        // when persisting and making sure we flushed and cleared all caches...
        persister.persist(stateMachine, leaveEntity);
        entityManager.flush();
        entityManager.clear();

        // then the state is set on the order.
        leaveEntity = repository.getOne(leaveEntity.getId());
        assertNotNull(leaveEntity.getStateMachineContext());
        assertEquals(leaveEntity.getCurrentState(), LeaveAppState.INITIAL);

        // and the statemachinecontext can be used to restore a new state
        // machine.
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachineNew = stateMachineFactory.getStateMachine();
        persister.restore(stateMachineNew, leaveEntity);
        assertEquals(stateMachineNew.getState().getId(), LeaveAppState.INITIAL);

        // and the repository should find one order by its current state.
        Page<LeaveEntity> leaveEntityPage = repository.findByCurrentState(LeaveAppState.INITIAL,
            PageRequest.of(0, 10));
        assertEquals(leaveEntityPage.getNumberOfElements(), 1);
    }

    @Test
    void saveEntity() {

        // given
        LeaveEntity leaveEntity = new LeaveEntity();
        leaveEntity.setCurrentState(LeaveAppState.UNDER_PROCESS);
        leaveEntity.setStateMachineContext(createStateMachineContext());
        leaveEntity.setId(1L);

        // when
        LeaveEntity savedLeaveEntity = entityManager.persist(leaveEntity);
        LeaveEntity fetchedLeaveEntity = entityManager.find(LeaveEntity.class, savedLeaveEntity.getId());
        StateMachineContext<LeaveAppState, LeaveAppEvent> savedSMC = savedLeaveEntity.getStateMachineContext();
        StateMachineContext<LeaveAppState, LeaveAppEvent> fetchedSMC = fetchedLeaveEntity.getStateMachineContext();

        // then
        assertAll("EntityId",
            () -> assertNotNull(savedLeaveEntity.getId(), "saved smc id should not be null"),
            () -> assertNotNull(fetchedLeaveEntity.getId(), "fetched smc id should not be null"),
            () -> assertEquals(savedLeaveEntity.getId(), fetchedLeaveEntity.getId()));

        assertAll("StateMachineContext",
            () -> assertNotNull(savedSMC),
            () -> assertNotNull(fetchedSMC));

        assertAll("SMC-State",
            () -> assertNotNull(savedSMC.getState(), "saved smc state should not be null"),
            () -> assertNotNull(fetchedSMC.getState(), "fetched smc state should not be null"),
            () -> assertEquals(savedSMC.getState(), fetchedSMC.getState()));

        assertAll("SMC-ExtendedState",
            () -> assertNotNull(savedSMC.getExtendedState(), "saved smc extended state should not be null"),
            () -> assertNotNull(fetchedSMC.getExtendedState(), "fetched smc extended state should not be null"),
            () -> assertEquals(savedSMC.getExtendedState(), fetchedSMC.getExtendedState()));

        assertAll("SMC-Event",
            () -> assertNotNull(savedSMC.getEvent(), "saved smc event should not be null"),
            () -> assertNotNull(fetchedSMC.getEvent(), "fetched smc event should not be null"),
            () -> assertEquals(savedSMC.getEvent(), fetchedSMC.getEvent()));

        assertEquals(savedSMC.getHistoryStates(), fetchedSMC.getHistoryStates());

        assertEquals(savedSMC.getChildReferences(), fetchedSMC.getChildReferences());
    }

    private StateMachineContext<LeaveAppState, LeaveAppEvent> createStateMachineContext() {
        ExtendedState extendedState = new DefaultExtendedState();
        Map<Object, Object> variablesMap = extendedState.getVariables();
        variablesMap.put(IS_PARALLEL, false);
        variablesMap.put(ROLL_BACK_COUNT, 0);
        variablesMap.put(RETURN_COUNT, 0);
        variablesMap.put(ONLY_FORWARD_WITH_APPROVAL, true);

        return new DefaultStateMachineContext<>(
            LeaveAppState.UNDER_PROCESS, LeaveAppEvent.TRIGGER_REVIEW_OF, Collections.emptyMap(),
            extendedState);
    }

}
