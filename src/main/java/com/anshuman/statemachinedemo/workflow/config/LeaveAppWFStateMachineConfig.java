package com.anshuman.statemachinedemo.workflow.config;


import com.anshuman.statemachinedemo.other.StateMachineMonitor;
import com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.guard.LeaveAppStateMachineGuards;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import java.util.EnumSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;



@Configuration
@EnableStateMachineFactory
@Slf4j
public class LeaveAppWFStateMachineConfig extends EnumStateMachineConfigurerAdapter<LeaveAppState, LeaveAppEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<LeaveAppState, LeaveAppEvent> config)
        throws Exception {
        config
            .withMonitoring()
                .monitor(new StateMachineMonitor<>())
                .and()
            .withConfiguration()
                .machineId("LeaveApplicationWorkflowStateMachineV1")
                .listener(new StateMachineListener<>());
    }

    @Override
    public void configure(StateMachineStateConfigurer<LeaveAppState, LeaveAppEvent> states)
        throws Exception {
        states
            .withStates()
                .initial(LeaveAppState.INITIAL, context -> LeaveAppStateMachineActions.initiateLeaveAppWorkflow(context.getExtendedState()))
                .end(LeaveAppState.COMPLETED)
                .states(EnumSet.allOf(LeaveAppState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
                .name("UserCreatesTheLeaveApplication")
                .source(LeaveAppState.INITIAL)
                .event(LeaveAppEvent.START)
                .target(LeaveAppState.CREATED)
                .and()
            .withExternal()
                .name("UserSubmitsTheCreatedTheLeaveApplication")
                .source(LeaveAppState.CREATED)
                .event(LeaveAppEvent.SUBMIT)
                .target(LeaveAppState.SUBMITTED)
                .and()
            .withExternal()
                .name("SystemTriggersTheSubmittedLeaveApplication")
                .source(LeaveAppState.SUBMITTED)
                .event(LeaveAppEvent.TRIGGER_REVIEW_OF)
                .target(LeaveAppState.UNDER_PROCESS)
                .and()
            .withExternal()
                .name("ReviewerRequestsChangesInTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.REQUEST_CHANGES_IN)
                .target(LeaveAppState.CREATED)
                .action(context -> LeaveAppStateMachineActions.returnBack(context.getExtendedState()))
                .and()
            .withExternal()
                .name("UserCancelsTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.CANCEL)
                .target(LeaveAppState.CLOSED)
                .action(context -> LeaveAppStateMachineActions.closeCancel(context.getExtendedState()))
                .and()
            .withExternal()
                .name("ReviewerApprovesTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.APPROVE)
                .target(LeaveAppState.CLOSED)
                .action(context -> LeaveAppStateMachineActions.closeApprove(context.getExtendedState()))
                .and()
            .withExternal()
                .name("ReviewerRejectsTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.REJECT)
                .guard(LeaveAppStateMachineGuards::cannotRollBackCanceledApplication)
                .target(LeaveAppState.CLOSED)
                .action(context -> LeaveAppStateMachineActions.closeReject(context.getExtendedState()))
                .and()
            .withExternal()
                .name("ReviewerRollsBackTheLeaveApplicationUnderReview")
                .source(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.ROLL_BACK)
                .guard(LeaveAppStateMachineGuards::cannotRollBackCanceledApplication)
                .target(LeaveAppState.UNDER_PROCESS)
                //.action(context -> LeaveAppStateMachineActions.rollBack(context.getExtendedState()))
                .and()
            .withExternal()
                .name("SystemCompletesTheLeaveApplication")
                .source(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.TRIGGER_COMPLETE)
                .target(LeaveAppState.COMPLETED);
    }


}