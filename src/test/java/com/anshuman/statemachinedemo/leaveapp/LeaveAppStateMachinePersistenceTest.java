package com.anshuman.statemachinedemo.leaveapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anshuman.statemachinedemo.App;
import com.anshuman.statemachinedemo.workflow.config.LeaveAppWFStateMachineConfig;
import com.anshuman.statemachinedemo.workflow.config.StateMachinePersistenceConfig;
import com.anshuman.statemachinedemo.workflow.model.entity.LeaveAppEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.persist.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = {App.class, LeaveAppWFStateMachineConfig.class, StateMachinePersistenceConfig.class})
class LeaveAppStateMachinePersistenceTest {

//    @Autowired
//    private LeaveAppRepository leaveAppRepository;

    @Autowired
    DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, LeaveAppEntity> stateMachineAdapter;

    /*@Test
    @Transactional
    void testCreatingLeaveAppEntity() throws Exception {
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine;

        // Given a leave application entity
        LeaveAppEntity leaveAppEntity1 = new LeaveAppEntity();
        leaveAppEntity1.setId(1L);
        stateMachine = getStateMachine(stateMachineAdapter.create());
        boolean eventSentSuccessfully = ReactiveHelper.eventsSentSuccessfully(stateMachine, LeaveAppEvent.START);
        assertTrue(eventSentSuccessfully);
        stateMachineAdapter.persist(stateMachine, leaveAppEntity1);

        LeaveAppEntity leaveAppEntity2 = new LeaveAppEntity();
        leaveAppEntity2.setId(2L);
        stateMachine = getStateMachine(stateMachineAdapter.create());
        eventSentSuccessfully = ReactiveHelper.eventsSentSuccessfully(stateMachine, LeaveAppEvent.START, LeaveAppEvent.SUBMIT);
        assertTrue(eventSentSuccessfully);
        stateMachineAdapter.persist(stateMachine, leaveAppEntity2);

        // when we persist it
        List<LeaveAppEntity> savedLeaveEntities = leaveAppRepository.saveAllAndFlush(List.of(leaveAppEntity1, leaveAppEntity2));

        // then it should be persisted in the data store with the state machine context.
        LeaveAppEntity savedLeaveAppEntity1 = savedLeaveEntities.stream().filter(le -> le.getId().equals(1L)).findFirst().orElse(null);
        assertNotNull(savedLeaveAppEntity1);
        assertNotNull(savedLeaveAppEntity1.getStateMachineContext());
        assertEquals(LeaveAppState.CREATED, savedLeaveAppEntity1.getCurrentState());
        stateMachine = getStateMachine(stateMachineAdapter.restore(savedLeaveAppEntity1));
        assertEquals(LeaveAppState.CREATED, stateMachine.getState().getId());

        LeaveAppEntity savedLeaveEntity2 = savedLeaveEntities.stream().filter(le -> le.getId().equals(2L)).findFirst().orElse(null);
        assertNotNull(savedLeaveEntity2);
        assertNotNull(savedLeaveEntity2.getStateMachineContext());
        assertEquals(LeaveAppState.SUBMITTED, savedLeaveEntity2.getCurrentState());
        stateMachine = getStateMachine(stateMachineAdapter.restore(savedLeaveEntity2));
        assertEquals(LeaveAppState.SUBMITTED, stateMachine.getState().getId());
    }*/

    private StateMachine<LeaveAppState, LeaveAppEvent> getStateMachine(StateMachine<LeaveAppState, LeaveAppEvent> stateMachine) {
        stateMachine.startReactively().block();
        return stateMachine;
    }

}
