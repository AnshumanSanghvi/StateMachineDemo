package com.anshuman.statemachinedemo.other;

import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeApprove;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeCancel;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.closeReject;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.initiateLeaveAppWorkflow;
import static com.anshuman.statemachinedemo.workflow.action.LeaveAppStateMachineActions.returnBack;

import com.anshuman.statemachinedemo.workflow.config.StateMachineListener;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.guard.LeaveAppStateMachineGuards;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import java.util.EnumSet;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class LeaveAppStateMachineBuilder {

    private StateMachine<LeaveAppState, LeaveAppEvent> createStateMachine(String workflowName, int totalReviewers) throws Exception {
        Builder<LeaveAppState, LeaveAppEvent> builder = StateMachineBuilder.builder();

        configureConfigurations(builder, workflowName);

        configureStates(builder);

        configureTransitions(builder, totalReviewers);

        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = builder.build();

        stateMachine
            .getStateMachineAccessor()
            .withAllRegions()
            .forEach(access -> access.addStateMachineInterceptor(new StateMachineInterceptor<>()));

        return stateMachine;
    }

    private static void configureConfigurations(Builder<LeaveAppState, LeaveAppEvent> builder, String workflowName)
        throws Exception {
        builder
            .configureConfiguration()
                .withMonitoring()
                    .monitor(new StateMachineMonitor<>())
                    .and()
                .withConfiguration()
                    .machineId(workflowName)
                    .listener(new StateMachineListener<>());
    }

    private static void configureStates(Builder<LeaveAppState, LeaveAppEvent> builder) throws Exception {
        builder
            .configureStates()
                .withStates()
                    .initial(LeaveAppState.INITIAL, context -> initiateLeaveAppWorkflow(context.getExtendedState()))
                    .end(LeaveAppState.COMPLETED)
                    .states(EnumSet.allOf(LeaveAppState.class));
    }

    private static void configureTransitions(Builder<LeaveAppState, LeaveAppEvent> builder, int totalReviewers) throws Exception {
        StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitionConfigurer = builder.configureTransitions();

            transitionConfigurer.withExternal()
                .name("UserCreatesTheLeaveApplication")
                .source(LeaveAppState.INITIAL)
                .event(LeaveAppEvent.START)
                .target(LeaveAppState.CREATED)
                .and()
            .withExternal()
                .name("UserSubmitsTheCreatedLeaveApplication")
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
                .action(context -> returnBack(context.getExtendedState()))
                .and()
            .withExternal()
                .name("UserCancelsTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.CANCEL)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeCancel(context.getExtendedState()))
                .and()
            .withExternal()
                .name("ReviewerApprovesTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.APPROVE)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeApprove(context.getExtendedState()))
                .and()
            .withExternal()
                .name("ReviewerRejectsTheLeaveApplicationUnderReview")
                .source(LeaveAppState.UNDER_PROCESS)
                .event(LeaveAppEvent.REJECT)
                .guard(LeaveAppStateMachineGuards::cannotRollBackCanceledApplication)
                .target(LeaveAppState.CLOSED)
                .action(context -> closeReject(context.getExtendedState()))
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

    private void configureReviewers(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> transitionConfigurer, int totalReviewers) throws Exception {

        while(totalReviewers > 0) {
            transitionConfigurer.withExternal();
        }

    }
}

