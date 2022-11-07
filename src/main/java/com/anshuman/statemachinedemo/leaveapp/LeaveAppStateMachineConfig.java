package com.anshuman.statemachinedemo.leaveapp;


import com.anshuman.statemachinedemo.util.StringUtil;
import java.util.EnumSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;


@Configuration
@EnableStateMachine
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
                .initial(LeaveAppState.CREATED)
                .end(LeaveAppState.CLOSED)
                .states(EnumSet.allOf(LeaveAppState.class));

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
                .source(LeaveAppState.CREATED)
                .target(LeaveAppState.SUBMITTED)
                .event(LeaveAppEvent.SUBMIT)
                .and()
            .withExternal()
                .source(LeaveAppState.SUBMITTED)
                .target(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.TRIGGER_REVIEW_OF)
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .target(LeaveAppState.CREATED)
                .event(LeaveAppEvent.REQUEST_CHANGES_IN)
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .target(LeaveAppState.CANCELED)
                .event(LeaveAppEvent.CANCEL)
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .target(LeaveAppState.APPROVED)
                .event(LeaveAppEvent.APPROVE)
                .and()
            .withExternal()
                .source(LeaveAppState.APPROVED)
                .target(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.ROLL_BACK_APPROVAL)
                .and()
            .withExternal()
                .source(LeaveAppState.REJECTED)
                .target(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.ROLL_BACK_REJECTION)
                .and()
            .withExternal()
                .source(LeaveAppState.UNDER_PROCESS)
                .target(LeaveAppState.REJECTED)
                .event(LeaveAppEvent.REJECT)
                .and()
            .withExternal()
                .source(LeaveAppState.REJECTED)
                .target(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(LeaveAppState.APPROVED)
                .target(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(LeaveAppState.CANCELED)
                .target(LeaveAppState.CLOSED)
                .event(LeaveAppEvent.TRIGGER_CLOSE);
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
                log.trace("State Context: {} {} {} {}",
                    StringUtil.stageFromContext(stateContext),
                    StringUtil.sourceStateFromContext(stateContext),
                    StringUtil.eventFromContext(stateContext),
                    StringUtil.targetStateFromContext(stateContext));
            }
        };
    }
}