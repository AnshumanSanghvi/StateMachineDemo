package com.anshuman.workflow.statemachine.config;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.*;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.*;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_CLOSED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_COMPLETED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_CREATED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_INITIAL;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_SUBMITTED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_UNDER_PROCESS;

import com.anshuman.workflow.statemachine.action.LeaveAppActions.TransitionActions;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.guard.LeaveAppGuards;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory(name = "LeaveAppStateMachineFactory")
public class LeaveAppSMConfig extends EnumStateMachineConfigurerAdapter<LeaveAppState, LeaveAppEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<LeaveAppState, LeaveAppEvent> config) throws Exception {
        config
            .withConfiguration()
                .machineId(LEAVE_APP_WF_V1)
                .listener(new StateMachineListener<>())
                .and()
            .withMonitoring()
                .monitor(new StateMachineMonitor<>())
                .and();
    }

    @Override
    public void configure(StateMachineStateConfigurer<LeaveAppState, LeaveAppEvent> states) throws Exception {
        states
            .withStates()
                .initial(S_INITIAL)
                .states(Set.of(S_CREATED, S_SUBMITTED, S_UNDER_PROCESS, S_CLOSED))
                .end(S_COMPLETED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitions) throws Exception {

        // initialize
        transitions
            .withExternal()
                .name(TX_USER_CREATES_LEAVE_APP)
                .source(S_INITIAL)
                .event(E_INITIALIZE)
                .target(S_CREATED)
                .and();

        // submit application
        transitions
            .withExternal()
                .name(TX_USER_SUBMITS_LEAVE_APP)
                .source(S_CREATED)
                .event(E_SUBMIT)
                .target(S_SUBMITTED)
                .and();

        // move application to be reviewed
        transitions
            .withExternal()
                .name(TX_SYSTEM_TRIGGERS_LEAVE_APP_FOR_REVIEW)
                .source(S_SUBMITTED)
                .event(E_TRIGGER_REVIEW_OF)
                .target(S_UNDER_PROCESS)
                .action(TransitionActions::autoTriggerReview)
                .and();

        // cancel
        transitions
            .withExternal()
                .name(TX_USER_CANCELS_CREATED_LEAVE_APP)
                .source(S_CREATED)
                .event(E_CANCEL)
                .target(S_COMPLETED)
                .action(TransitionActions::cancel)
                .and()
            .withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_UNDER_REVIEW)
                .source(S_UNDER_PROCESS)
                .event(E_CANCEL)
                .target(S_COMPLETED)
                .action(TransitionActions::cancel)
                .and();

        // forward and approve
        transitions
            .withInternal()
                .name(TX_REVIEWER_FORWARDS_APPLICATION)
                .source(S_UNDER_PROCESS)
                .event(E_FORWARD)
                .guard(LeaveAppGuards::forward)
                .action(TransitionActions::forward)
                .and()
            .withExternal()
                .name(TX_REVIEWER_APPROVES_LEAVE_APP_IN_SERIAL_FLOW)
                .source(S_UNDER_PROCESS)
                .event(E_APPROVE)
                .target(S_CLOSED)
                .guard(LeaveAppGuards::approve)
                .action(TransitionActions::approve)
                .and();

        // request changes
        transitions
            .withExternal()
                .name(TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_SERIAL_FLOW)
                .source(S_UNDER_PROCESS)
                .event(E_REQUEST_CHANGES_IN)
                .target(S_CREATED)
                .action(TransitionActions::requestChanges)
                .and();

        // reject
        transitions
            .withExternal()
                .name(TX_REVIEWER_REJECTS_LEAVE_APP_IN_SERIAL_FLOW)
                .source(S_UNDER_PROCESS)
                .event(E_REJECT)
                .target(S_CLOSED)
                .action(TransitionActions::reject)
                .and();

        // roll back approval
        transitions
            .withInternal()
                .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
                .source(S_UNDER_PROCESS)
                .event(E_ROLL_BACK)
                .guard(LeaveAppGuards::rollBackApproval)
                .action(TransitionActions::rollBackApproval)
                .and()
            .withExternal()
                .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
                .source(S_CLOSED)
                .event(E_ROLL_BACK)
                .target(S_UNDER_PROCESS)
                .guard(LeaveAppGuards::rollBackApproval)
                .action(TransitionActions::rollBackApproval)
                .and();

        // complete
        transitions
            .withExternal()
                .name(TX_SYSTEM_COMPLETES_LEAVE_APP)
                .source(S_CLOSED)
                .event(E_TRIGGER_COMPLETE)
                .target(S_COMPLETED)
                .and();

    }
}
