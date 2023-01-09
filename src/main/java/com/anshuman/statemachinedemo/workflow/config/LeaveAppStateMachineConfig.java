package com.anshuman.statemachinedemo.workflow.config;


import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeApprove;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeCancel;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeReject;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.initiateLeaveAppWorkflow;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.returnBack;
import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.*;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.APPROVE;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.CANCEL;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.REJECT;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.REQUEST_CHANGES_IN;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.START;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.SUBMIT;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.TRIGGER_COMPLETE;
import static com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent.TRIGGER_REVIEW_OF;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.CLOSED;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.COMPLETED;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.CREATED;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.INITIAL;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.SUBMITTED;
import static com.anshuman.statemachinedemo.workflow.state.LeaveAppState.UNDER_PROCESS;

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
public class LeaveAppStateMachineConfig extends EnumStateMachineConfigurerAdapter<LeaveAppState, LeaveAppEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<LeaveAppState, LeaveAppEvent> config)
        throws Exception {
        config
            .withMonitoring()
                .monitor(new StateMachineMonitor<>())
                .and()
            .withConfiguration()
                .machineId(LEAVE_APP_WF_V1)
                .listener(new StateMachineListener<>());
    }

    @Override
    public void configure(StateMachineStateConfigurer<LeaveAppState, LeaveAppEvent> states)
        throws Exception {
        states
            .withStates()
                .initial(INITIAL, context -> initiateLeaveAppWorkflow(context.getExtendedState()))
                .end(COMPLETED)
                .states(EnumSet.allOf(LeaveAppState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
                .name(CREATE_LEAVE_APP)
                .source(INITIAL)
                .event(START)
                .target(CREATED)
                .and()
            .withExternal()
                .name(SUBMIT_CREATED_LEAVE_APP)
                .source(CREATED)
                .event(SUBMIT)
                .target(SUBMITTED)
                .and()
            .withExternal()
                .name(TRIGGER_LEAVE_APP_REVIEW)
                .source(SUBMITTED)
                .event(TRIGGER_REVIEW_OF)
                .target(UNDER_PROCESS)
                .and()
            .withExternal()
                .name(REQUEST_CHANGES_IN_LEAVE_APP)
                .source(UNDER_PROCESS)
                .event(REQUEST_CHANGES_IN)
                .target(CREATED)
                .action(context -> returnBack(context.getExtendedState()))
                .and()
            .withExternal()
                .name(CANCEL_LEAVE_APP)
                .source(UNDER_PROCESS)
                .event(CANCEL)
                .target(CLOSED)
                .action(context -> closeCancel(context.getExtendedState()))
                .and()
            .withExternal()
                .name(APPROVE_LEAVE_APP)
                .source(UNDER_PROCESS)
                .event(APPROVE)
                .target(CLOSED)
                .action(context -> closeApprove(context.getExtendedState()))
                .and()
            .withExternal()
                .name(REJECT_LEAVE_APP)
                .source(UNDER_PROCESS)
                .event(REJECT)
                .guard(LeaveAppStateMachineGuards::cannotRollBackCanceledApplication)
                .target(CLOSED)
                .action(context -> closeReject(context.getExtendedState()))
                .and()
            .withExternal()
                .name(ROLL_BACK_LEAVE_APP)
                .source(CLOSED)
                .event(LeaveAppEvent.ROLL_BACK)
                .guard(LeaveAppStateMachineGuards::cannotRollBackCanceledApplication)
                .target(UNDER_PROCESS)
                //.action(context -> LeaveAppStateMachineActions.rollBack(context.getExtendedState()))
                .and()
            .withExternal()
                .name(COMPLETE_LEAVE_APP)
                .source(CLOSED)
                .event(TRIGGER_COMPLETE)
                .target(COMPLETED);
    }


}