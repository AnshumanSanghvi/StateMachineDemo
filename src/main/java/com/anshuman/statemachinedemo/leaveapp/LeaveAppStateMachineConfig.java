package com.anshuman.statemachinedemo.leaveapp;


import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.closeApprove;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.closeCancel;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.closeReject;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.initiateLeaveAppWorkflow;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.returnBack;
import static com.anshuman.statemachinedemo.leaveapp.LeaveAppStateMachineActions.rollBack;

import com.anshuman.statemachinedemo.util.StringUtil;
import java.util.EnumSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;


@Configuration
@EnableStateMachineFactory
@Slf4j
public class LeaveAppStateMachineConfig extends EnumStateMachineConfigurerAdapter<LeaveAppState, LeaveAppEvent> {


    @Override
    public void configure(StateMachineConfigurationConfigurer<LeaveAppState, LeaveAppEvent> config)
        throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<LeaveAppState, LeaveAppEvent> states)
        throws Exception {
        states
            .withStates()
                .initial(LeaveAppState.CREATED, context -> initiateLeaveAppWorkflow(context.getExtendedState()))
//                .end(LeaveAppState.CLOSED)
                .states(EnumSet.allOf(LeaveAppState.class));

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
                .source(LeaveAppState.INITIAL)
                .event(LeaveAppEvent.START)
                .source(LeaveAppState.CREATED)
                .and()
            .withExternal()
                .source(LeaveAppState.CREATED)
                .event(LeaveAppEvent.SUBMIT)
                .target(LeaveAppState.SUBMITTED)
                .and()
            .withExternal()
                .source(LeaveAppState.SUBMITTED)
                .event(LeaveAppEvent.TRIGGER_REVIEW_OF)
                .target(LeaveAppState.UNDER_PROCESS)
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.REQUEST_CHANGES_IN)
                .target(LeaveAppState.CREATED)
                .action(context -> returnBack(context.getExtendedState()))
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.CANCEL)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeCancel(context.getExtendedState()))
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.APPROVE)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeApprove(context.getExtendedState()))
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.REJECT)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeReject(context.getExtendedState()))
                .and()
            .withExternal()
                .source(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.ROLL_BACK)
                .target(LeaveAppState.UNDER_PROCESS)
                .action(context -> rollBack(context.getExtendedState()))
                .and();
    }

    @Bean
    public StateMachineListener<LeaveAppState, LeaveAppEvent> listener() {
        return new StateMachineListenerAdapter<>() {

            @Override
            public void transition(Transition<LeaveAppState, LeaveAppEvent> transition) {
                log.debug("Transitioning: {} {}",
                    StringUtil.sourceStateFromTransition(transition),
                    StringUtil.targetStateFromTransition(transition));
            }

            @Override
            public void stateContext(StateContext<LeaveAppState, LeaveAppEvent> stateContext) {
                log.trace("State Context: {} {} {} {} {}",
                    StringUtil.stageFromContext(stateContext),
                    StringUtil.sourceStateFromContext(stateContext),
                    StringUtil.eventFromContext(stateContext),
                    StringUtil.targetStateFromContext(stateContext),
                    StringUtil.extendedStateFromContext(stateContext));
            }

            @Override
            public void eventNotAccepted(Message<LeaveAppEvent> event) {
                if (event == null) {
                    log.error("Event not accepted.");
                    return;
                }
                log.trace("Event not accepted. Event Headers: {}", StringUtil.messageHeaders(event.getHeaders()));
                log.error("Event not accepted. Event Payload: {}", event.getPayload());
            }
        };
    }
}