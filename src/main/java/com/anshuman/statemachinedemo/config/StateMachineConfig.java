package com.anshuman.statemachinedemo.config;


import com.anshuman.statemachinedemo.config.StateMachineConfig.AppEvent;
import com.anshuman.statemachinedemo.config.StateMachineConfig.AppState;
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
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<AppState, AppEvent> {

    public enum AppEvent {
        START, SUBMIT, TRIGGER_REVIEW_OF, REQUEST_CHANGES_IN, APPROVE, REJECT, CANCEL, ROLL_BACK_APPROVAL, ROLL_BACK_REJECTION, TRIGGER_CLOSE
    }

    public enum AppState {
        INITIAL, CREATED, SUBMITTED, UNDER_PROCESS, APPROVED, REJECTED, CANCELED, CLOSED
    }



    @Override
    public void configure(StateMachineConfigurationConfigurer<AppState, AppEvent> config)
        throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<AppState, AppEvent> states)
        throws Exception {
        states
            .withStates()
                .initial(AppState.CREATED)
                .end(AppState.CLOSED)
                .states(EnumSet.allOf(AppState.class));

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AppState, AppEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
                .source(AppState.CREATED)
                .target(AppState.SUBMITTED)
                .event(AppEvent.SUBMIT)
                .and()
            .withExternal()
                .source(AppState.SUBMITTED)
                .target(AppState.UNDER_PROCESS)
                .event(AppEvent.TRIGGER_REVIEW_OF)
                .and()
            .withExternal()
                .source(AppState.UNDER_PROCESS)
                .target(AppState.CREATED)
                .event(AppEvent.REQUEST_CHANGES_IN)
                .and()
            .withExternal()
                .source(AppState.UNDER_PROCESS)
                .target(AppState.CANCELED)
                .event(AppEvent.CANCEL)
                .and()
            .withExternal()
                .source(AppState.UNDER_PROCESS)
                .target(AppState.APPROVED)
                .event(AppEvent.APPROVE)
                .and()
            .withExternal()
                .source(AppState.APPROVED)
                .target(AppState.UNDER_PROCESS)
                .event(AppEvent.ROLL_BACK_APPROVAL)
                .and()
            .withExternal()
                .source(AppState.REJECTED)
                .target(AppState.UNDER_PROCESS)
                .event(AppEvent.ROLL_BACK_REJECTION)
                .and()
            .withExternal()
                .source(AppState.UNDER_PROCESS)
                .target(AppState.REJECTED)
                .event(AppEvent.REJECT)
                .and()
            .withExternal()
                .source(AppState.REJECTED)
                .target(AppState.CLOSED)
                .event(AppEvent.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(AppState.APPROVED)
                .target(AppState.CLOSED)
                .event(AppEvent.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(AppState.CANCELED)
                .target(AppState.CLOSED)
                .event(AppEvent.TRIGGER_CLOSE);
    }

    @Bean
    public StateMachineListener<AppState, AppEvent> listener() {
        return new StateMachineListenerAdapter<>() {

            @Override
            public void transition(Transition<AppState, AppEvent> transition) {
                log.debug("Transitioning: {} {}",
                    StringUtil.sourceStateFromTransition(transition),
                    StringUtil.targetStateFromTransition(transition));
            }

            @Override
            public void stateContext(StateContext<AppState, AppEvent> stateContext) {
                log.trace("State Context: {} {} {} {}",
                    StringUtil.stageFromContext(stateContext),
                    StringUtil.sourceStateFromContext(stateContext),
                    StringUtil.eventFromContext(stateContext),
                    StringUtil.targetStateFromContext(stateContext));
            }
        };
    }
}