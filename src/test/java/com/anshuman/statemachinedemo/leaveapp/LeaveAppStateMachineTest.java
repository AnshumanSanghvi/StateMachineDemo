package com.anshuman.statemachinedemo.leaveapp;

import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.APPROVED;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.CLOSED_STATE;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.IS_PARALLEL;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.ONLY_FORWARD_WITH_APPROVAL;
import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.RETURN_COUNT;

import com.anshuman.statemachinedemo.workflow.config.LeaveAppWFStateMachineConfig;
import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LeaveAppWFStateMachineConfig.class)
class LeaveAppStateMachineTest {

    @Autowired
    private StateMachineFactory<LeaveAppState, LeaveAppEvent> stateMachineFactory;

    private static final int DEFAULT_AWAIT_TIME = 2;


    @Test
    void testStateMachine() throws Exception {
        var testPlan = StateMachineTestPlanBuilder
            .<LeaveAppState, LeaveAppEvent>builder()
            .defaultAwaitTime(DEFAULT_AWAIT_TIME)
            .stateMachine(stateMachineFactory.getStateMachine("LeaveApplicationWorkflowStateMachineV1"))
            .step() // step 0
            .expectState(LeaveAppState.INITIAL)
            .expectVariable(IS_PARALLEL, false)
            .expectVariable(ONLY_FORWARD_WITH_APPROVAL, true)
            .and()
            .step() // step 1
            .sendEvent(LeaveAppEvent.START)
            .expectState(LeaveAppState.CREATED)
            .and()
            .step() // step 2
            .sendEvent(LeaveAppEvent.SUBMIT)
            .expectState(LeaveAppState.SUBMITTED)
            .and()
            .step() // step 3
            .sendEvent(LeaveAppEvent.TRIGGER_REVIEW_OF)
            .expectState(LeaveAppState.UNDER_PROCESS)
            .and()
            .step() // step 4
            .sendEvent(LeaveAppEvent.REQUEST_CHANGES_IN)
            .expectState(LeaveAppState.CREATED)
            .expectVariable(RETURN_COUNT, 1)
            .and()
            .step() // step 5
            .sendEvent(LeaveAppEvent.SUBMIT)
            .expectState(LeaveAppState.SUBMITTED)
            .and()
            .step() // step 6
            .sendEvent(LeaveAppEvent.TRIGGER_REVIEW_OF)
            .expectState(LeaveAppState.UNDER_PROCESS)
            .and()
            .step() // step 7
            .sendEvent(LeaveAppEvent.APPROVE)
            .expectState(LeaveAppState.CLOSED)
            .expectVariable(CLOSED_STATE, APPROVED)
            .and()
            .step() // step 8
            .sendEvent(LeaveAppEvent.TRIGGER_COMPLETE)
            .expectState(LeaveAppState.COMPLETED)
            .and()
            .build();

        testPlan.test();
    }

}
