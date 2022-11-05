package com.anshuman.statemachinedemo.config;


import com.anshuman.statemachinedemo.config.StateMachineConfig.AppEvents;
import com.anshuman.statemachinedemo.config.StateMachineConfig.AppStates;
import java.util.EnumSet;
import java.util.Optional;
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
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<AppStates, AppEvents> {

    public enum AppEvents {
        START, SUBMIT, TRIGGER_REVIEW_OF, REQUEST_CHANGES_IN, APPROVE, REJECT, CANCEL, ROLL_BACK_APPROVAL, ROLL_BACK_REJECTION, TRIGGER_CLOSE
    }

    public enum AppStates {
        INITIAL, CREATED, SUBMITTED, UNDER_PROCESS, APPROVED, REJECTED, CANCELED, CLOSED
    }



    @Override
    public void configure(StateMachineConfigurationConfigurer<AppStates, AppEvents> config)
        throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<AppStates, AppEvents> states)
        throws Exception {
        states
            .withStates()
                .initial(AppStates.CREATED)
                .end(AppStates.CLOSED)
                .states(EnumSet.allOf(AppStates.class));


    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AppStates, AppEvents> transitions)
        throws Exception {
        transitions
            .withExternal()
                .source(AppStates.CREATED)
                .target(AppStates.SUBMITTED)
                .event(AppEvents.SUBMIT)
                .and()
            .withExternal()
                .source(AppStates.SUBMITTED)
                .target(AppStates.UNDER_PROCESS)
                .event(AppEvents.TRIGGER_REVIEW_OF)
                .and()
            .withExternal()
                .source(AppStates.UNDER_PROCESS)
                .target(AppStates.CREATED)
                .event(AppEvents.REQUEST_CHANGES_IN)
                .and()
            .withExternal()
                .source(AppStates.UNDER_PROCESS)
                .target(AppStates.CANCELED)
                .event(AppEvents.CANCEL)
                .and()
            .withExternal()
                .source(AppStates.UNDER_PROCESS)
                .target(AppStates.APPROVED)
                .event(AppEvents.APPROVE)
                .and()
            .withExternal()
                .source(AppStates.APPROVED)
                .target(AppStates.UNDER_PROCESS)
                .event(AppEvents.ROLL_BACK_APPROVAL)
                .and()
            .withExternal()
                .source(AppStates.REJECTED)
                .target(AppStates.UNDER_PROCESS)
                .event(AppEvents.ROLL_BACK_REJECTION)
                .and()
            .withExternal()
                .source(AppStates.UNDER_PROCESS)
                .target(AppStates.REJECTED)
                .event(AppEvents.REJECT)
                .and()
            .withExternal()
                .source(AppStates.REJECTED)
                .target(AppStates.CLOSED)
                .event(AppEvents.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(AppStates.APPROVED)
                .target(AppStates.CLOSED)
                .event(AppEvents.TRIGGER_CLOSE)
                .and()
            .withExternal()
                .source(AppStates.CANCELED)
                .target(AppStates.CLOSED)
                .event(AppEvents.TRIGGER_CLOSE);
    }

    @Bean
    public StateMachineListener<AppStates, AppEvents> listener() {
        return new StateMachineListenerAdapter<>() {

            @Override
            public void transition(Transition<AppStates, AppEvents> transition) {
                log.debug("Transitioning: {} {}",
                    Optional
                        .ofNullable(transition)
                        .map(Transition::getSource)
                        .map(t -> " from: " + t.getId())
                        .orElse(""),
                    Optional
                        .ofNullable(transition)
                        .map(Transition::getTarget)
                        .map(t -> " to: " + t.getId())
                        .orElse(""));
            }

            @Override
            public void stateContext(StateContext<AppStates, AppEvents> stateContext) {
                log.trace("State Context: {} {} {} {}",
                    Optional
                        .ofNullable(stateContext.getStage())
                        .flatMap(stage -> Optional
                            .of(stage.name())
                            .map(st -> " Stage: " + st)
                        ).orElse(""),
                    Optional
                        .ofNullable(stateContext.getSource())
                        .map(st -> " | Source: " + st.getId())
                        .orElse(""),
                    Optional
                        .ofNullable(stateContext.getEvent())
                        .map(e ->" | Event: " + e)
                        .orElse(""),
                    Optional
                        .ofNullable(stateContext.getTarget())
                        .map(st -> " | Target: " + st.getId())
                        .orElse(""));
            }
        };
    }
}