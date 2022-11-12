package com.anshuman.statemachinedemo.leaveapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.anshuman.statemachinedemo.App;
import com.anshuman.statemachinedemo.workflow.config.LeaveAppWFStateMachineConfig;
import com.anshuman.statemachinedemo.workflow.config.StateMachinePersistenceConfig;
import com.anshuman.statemachinedemo.workflow.entity.LeaveAppEntity;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.persist.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.workflow.repository.LeaveAppRepository;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ContextConfiguration(classes = {App.class, LeaveAppWFStateMachineConfig.class, StateMachinePersistenceConfig.class})
class LeaveAppStateMachinePersistenceTest {

    @Autowired
    private LeaveAppRepository leaveAppRepository;

    @Autowired
    DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, LeaveAppEntity> stateMachineAdapter;

    @Test
    @Transactional
    void testCreatingLeaveAppEntity() throws Exception {
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

        // Given a leave application entity
        LeaveAppEntity leaveAppEntity1 = new LeaveAppEntity();
        leaveAppEntity1.setId(1L);
        stateMachine = stateMachineAdapter.create();
        stateMachine.sendEvent(LeaveAppEvent.START);
        stateMachineAdapter.persist(stateMachine, leaveAppEntity1);

        LeaveAppEntity leaveAppEntity2 = new LeaveAppEntity();
        leaveAppEntity2.setId(2L);
        stateMachine = stateMachineAdapter.create();
        stateMachine.sendEvent(LeaveAppEvent.START);
        stateMachine.sendEvent(LeaveAppEvent.SUBMIT);
        stateMachineAdapter.persist(stateMachine, leaveAppEntity2);

        // when we persist it
        List<LeaveAppEntity> savedLeaveEntities = leaveAppRepository.saveAllAndFlush(List.of(leaveAppEntity1, leaveAppEntity2));

        // then it should be persisted in the data store with the state machine context.
        LeaveAppEntity savedLeaveAppEntity1 = savedLeaveEntities.stream().filter(le -> le.getId().equals(1L)).findFirst().orElse(null);
        assertNotNull(savedLeaveAppEntity1);
        assertNotNull(savedLeaveAppEntity1.getStateMachineContext());
        assertEquals(LeaveAppState.CREATED, savedLeaveAppEntity1.getCurrentState());
        stateMachine = stateMachineAdapter.restore(savedLeaveAppEntity1);
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());

        LeaveAppEntity savedLeaveEntity2 = savedLeaveEntities.stream().filter(le -> le.getId().equals(2L)).findFirst().orElse(null);
        assertNotNull(savedLeaveEntity2);
        assertNotNull(savedLeaveEntity2.getStateMachineContext());
        assertEquals(LeaveAppState.SUBMITTED, savedLeaveEntity2.getCurrentState());
        stateMachine = stateMachineAdapter.restore(savedLeaveEntity2);
        assertEquals(LeaveAppState.SUBMITTED, stateMachine.getState().getId());
    }

}
