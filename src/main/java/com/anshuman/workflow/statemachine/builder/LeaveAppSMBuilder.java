package com.anshuman.workflow.statemachine.builder;


import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.*;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.*;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_APPROVAL_JUNCTION;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_CLOSED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_COMPLETED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_CREATED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_INITIAL;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_PARALLEL_APPROVAL_FLOW;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_SERIAL_APPROVAL_FLOW;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_SUBMITTED;
import static com.anshuman.workflow.statemachine.state.LeaveAppState.S_UNDER_PROCESS;

import com.anshuman.workflow.statemachine.action.LeaveAppActions.StateActions;
import com.anshuman.workflow.statemachine.action.LeaveAppActions.TransitionActions;
import com.anshuman.workflow.statemachine.config.StateMachineInterceptor;
import com.anshuman.workflow.statemachine.config.StateMachineListener;
import com.anshuman.workflow.statemachine.config.StateMachineMonitor;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.guard.LeaveAppGuards;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;



@Slf4j
public class LeaveAppSMBuilder {

    private LeaveAppSMBuilder() {
        // use class statically
    }

    public static StateMachine<LeaveAppState, LeaveAppEvent> createStateMachine(String stateMachineName, int reviewerCount, Map<Integer, Long> reviewerMap,
        boolean isParallel, int maxChangeRequests, int maxRollBackCount) throws Exception {
        Builder<LeaveAppState, LeaveAppEvent> builder = StateMachineBuilder.builder();

        createSMConfig(builder, stateMachineName);

        createSMStateConfig(builder, reviewerCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        createSMTransitionConfig(builder, reviewerMap, isParallel);

        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = builder.build();

        stateMachine
            .getStateMachineAccessor()
            .withAllRegions()
            .forEach(region -> region.addStateMachineInterceptor(new StateMachineInterceptor<>()));

        return stateMachine;
    }

    private static void createSMConfig(Builder<LeaveAppState, LeaveAppEvent> builder, String stateMachineName) throws Exception {
        builder
            .configureConfiguration()
            .withConfiguration()
                .machineId(stateMachineName)
                .listener(new StateMachineListener<>())
                .and()
            .withMonitoring()
                .monitor(new StateMachineMonitor<>())
                .and();
    }

    private static void createSMStateConfig(Builder<LeaveAppState, LeaveAppEvent> builder, int reviewersCount,
        Map<Integer, Long> reviewerMap, boolean isParallel, int maxChangeRequests, int maxRollBackCount) throws Exception {
        builder
            .configureStates()
            .withStates()
                .initial(S_INITIAL, context -> StateActions.initial(context, reviewersCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount))
                .states(Set.of(S_CREATED, S_SUBMITTED, S_UNDER_PROCESS))             
                .junction(S_APPROVAL_JUNCTION)
                .states(Set.of(S_SERIAL_APPROVAL_FLOW, S_PARALLEL_APPROVAL_FLOW))
                .state(S_CLOSED)
                .end(S_COMPLETED);
    }

    private static void createSMTransitionConfig(Builder<LeaveAppState, LeaveAppEvent> builder, Map<Integer, Long> reviewersMap, boolean isParallel)
        throws Exception {
        StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> configureTransitions = builder.configureTransitions();

        configureTransitions
            .withExternal()
                .name(TX_USER_CREATES_LEAVE_APP)
                .source(S_INITIAL).event(E_INITIALIZE).target(S_CREATED)
                .and()
            .withExternal()
                .name(TX_USER_SUBMITS_LEAVE_APP)
                .source(S_CREATED).event(E_SUBMIT).target(S_SUBMITTED)
                .and()
            .withExternal()
                .name(TX_SYSTEM_TRIGGERS_LEAVE_APP_FOR_REVIEW)
                .source(S_SUBMITTED).event(E_TRIGGER_REVIEW_OF).target(S_UNDER_PROCESS)
                .and()
            .withExternal()
                .name(TX_SYSTEM_TRIGGERS_APPROVAL_FLOW_JUNCTION)
                .source(S_UNDER_PROCESS).event(E_TRIGGER_FLOW_JUNCTION).target(S_APPROVAL_JUNCTION)
                .and()
            .withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_UNDER_REVIEW)
                .source(S_UNDER_PROCESS).event(E_CANCEL).target(S_COMPLETED)
                .action(TransitionActions::cancel)
                .and()
            .withJunction()
                .source(S_APPROVAL_JUNCTION)
                .first(S_PARALLEL_APPROVAL_FLOW, LeaveAppGuards::approvalFlowGuard).last(S_SERIAL_APPROVAL_FLOW)
                .and();

        if (isParallel)
            createSMParallelApprovalTransition(configureTransitions, reviewersMap);
        else
            createSMSerialApprovalTransition(configureTransitions, reviewersMap);

        configureTransitions
            .withExternal()
                .name(TX_SYSTEM_COMPLETES_LEAVE_APP)
                .source(S_CLOSED).event(LeaveAppEvent.E_TRIGGER_COMPLETE).target(S_COMPLETED)
                .and();
    }

    private static void createSMParallelApprovalTransition(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> configureTransitions,
        Map<Integer, Long> reviewersMap)
        throws Exception {
        configureTransitions
            .withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_CANCEL).target(S_COMPLETED)
                .action(TransitionActions::cancel)
                .and()
            .withExternal()
                .name(TX_REVIEWER_REJECTS_LEAVE_APP_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_REJECT).target(S_CLOSED)
                .action(TransitionActions::reject)
                .and()
            .withExternal()
                .name(TX_REVIEWER_APPROVES_LEAVE_APPLICATION_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_APPROVE).target(S_CLOSED)
                .guard(context -> LeaveAppGuards.approveInParallel(context, reviewersMap))
                .action(TransitionActions::approveInParallelFlow)
                .and()
            .withExternal()
                .name(TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_PARALLEL_FLOW)
                .source(S_CLOSED).event(E_REQUEST_CHANGES_IN).target(S_PARALLEL_APPROVAL_FLOW)
                .guard(LeaveAppGuards::requestChanges)
                .action(TransitionActions::requestChanges)
                .and();
    }

    private static void createSMSerialApprovalTransition(StateMachineTransitionConfigurer<LeaveAppState, LeaveAppEvent> configureTransitions, Map<Integer, Long> reviewersMap)
        throws Exception {

        configureTransitions.withInternal()
            .name(TX_REVIEWER_FORWARDS_APPLICATION)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_FORWARD)
            .guard(LeaveAppGuards::forward)
            .action(TransitionActions::forward);

        configureTransitions.withExternal()
            .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_ROLL_BACK).target(S_SERIAL_APPROVAL_FLOW)
            .guard(LeaveAppGuards::rollBackApproval)
            .action(TransitionActions::rollBackApproval);

        configureTransitions.withExternal()
            .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
            .source(S_CLOSED).event(E_ROLL_BACK).target(S_SERIAL_APPROVAL_FLOW)
            .guard(LeaveAppGuards::rollBackApproval)
            .action(TransitionActions::rollBackApproval);

        configureTransitions.withExternal()
            .name(TX_USER_CANCELS_LEAVE_APP_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_CANCEL).target(S_COMPLETED)
            .action(TransitionActions::cancel)
            .and();

        configureTransitions.withExternal()
            .name(TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_REQUEST_CHANGES_IN).target(S_SUBMITTED)
            .action(TransitionActions::requestChanges)
            .and();

        configureTransitions.withExternal()
            .name(TX_REVIEWER_REJECTS_LEAVE_APP_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_REJECT).target(S_CLOSED)
            .action(TransitionActions::reject)
            .and();

        configureTransitions.withExternal()
            .name(TX_REVIEWER_APPROVES_LEAVE_APP_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_APPROVE).target(S_CLOSED)
            .action(TransitionActions::approve)
            .and();
    }

}