package com.anshuman.workflow.statemachine.builder;

import static com.anshuman.workflow.statemachine.data.constant.TestConstant.*;
import static com.anshuman.workflow.statemachine.event.TestEvent.*;
import static com.anshuman.workflow.statemachine.state.TestState.S_APPROVAL_JUNCTION;
import static com.anshuman.workflow.statemachine.state.TestState.S_CLOSED;
import static com.anshuman.workflow.statemachine.state.TestState.S_COMPLETED;
import static com.anshuman.workflow.statemachine.state.TestState.S_CREATED;
import static com.anshuman.workflow.statemachine.state.TestState.S_INITIAL;
import static com.anshuman.workflow.statemachine.state.TestState.S_PARALLEL_APPROVAL_FLOW;
import static com.anshuman.workflow.statemachine.state.TestState.S_SERIAL_APPROVAL_FLOW;
import static com.anshuman.workflow.statemachine.state.TestState.S_SUBMITTED;
import static com.anshuman.workflow.statemachine.state.TestState.S_UNDER_PROCESS;

import com.anshuman.workflow.statemachine.action.TestAction.StateAction;
import com.anshuman.workflow.statemachine.action.TestAction.TransitionAction;
import com.anshuman.workflow.statemachine.config.StateMachineInterceptor;
import com.anshuman.workflow.statemachine.config.StateMachineListener;
import com.anshuman.workflow.statemachine.event.TestEvent;
import com.anshuman.workflow.statemachine.guard.TestGuard;
import com.anshuman.workflow.statemachine.state.TestState;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;


@Slf4j
public class TestSMBuilder {

    private TestSMBuilder() {
        // use class statically
    }

    public static StateMachine<TestState, TestEvent> createStateMachine(String stateMachineName, int reviewerCount, Map<Integer, Long> reviewerMap,
        boolean isParallel, int maxChangeRequests, int maxRollBackCount) throws Exception {
        Builder<TestState, TestEvent> builder = StateMachineBuilder.builder();

        createSMConfig(builder, stateMachineName);

        createSMStateConfig(builder, reviewerCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount);

        createSMTransitionConfig(builder, reviewerMap, isParallel);

        StateMachine<TestState, TestEvent> stateMachine = builder.build();

        stateMachine
            .getStateMachineAccessor()
            .withAllRegions()
            .forEach(region -> region.addStateMachineInterceptor(new StateMachineInterceptor<>()));

        return stateMachine;
    }

    private static void createSMConfig(Builder<TestState, TestEvent> builder, String stateMachineName) throws Exception {
        builder
            .configureConfiguration()
                .withConfiguration()
                    .machineId(stateMachineName)
                    .listener(new StateMachineListener<>());
    }

    private static void createSMStateConfig(Builder<TestState, TestEvent> builder, int reviewersCount,
        Map<Integer, Long> reviewerMap, boolean isParallel, int maxChangeRequests, int maxRollBackCount) throws Exception {
        builder
            .configureStates()
                .withStates()
                    .initial(S_INITIAL, context -> StateAction.initial(context, reviewersCount, reviewerMap, isParallel, maxChangeRequests, maxRollBackCount))
                    .state(S_CREATED)
                    .state(S_SUBMITTED)
                    .state(S_UNDER_PROCESS)
                    .junction(S_APPROVAL_JUNCTION)
                    .states(Set.of(S_SERIAL_APPROVAL_FLOW, S_PARALLEL_APPROVAL_FLOW))
                    .state(S_CLOSED)
                    .end(S_COMPLETED);
    }

    private static void createSMTransitionConfig(Builder<TestState, TestEvent> builder, Map<Integer, Long> reviewersMap, boolean isParallel)
        throws Exception {
        StateMachineTransitionConfigurer<TestState, TestEvent> configureTransitions = builder.configureTransitions();

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
                .source(S_SUBMITTED).event(E_TRIGGER_REVIEW).target(S_UNDER_PROCESS)
                .and()
            .withExternal()
                .name(TX_SYSTEM_TRIGGERS_APPROVAL_FLOW_JUNCTION)
                .source(S_UNDER_PROCESS).event(E_TRIGGER_FLOW_JUNCTION).target(S_APPROVAL_JUNCTION)
                .and()
            .withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_UNDER_REVIEW)
                .source(S_UNDER_PROCESS).event(E_CANCEL).target(S_COMPLETED)
                .action(TransitionAction::cancel)
                .and()
            .withJunction()
                .source(S_APPROVAL_JUNCTION)
                .first(S_PARALLEL_APPROVAL_FLOW, TestGuard::approvalFlowGuard).last(S_SERIAL_APPROVAL_FLOW)
                .and();

        if (isParallel)
            createSMParallelApprovalTransition(configureTransitions, reviewersMap);
        else
            createSMSerialApprovalTransition(configureTransitions, reviewersMap);

        configureTransitions
            .withExternal()
                .name(TX_SYSTEM_COMPLETES_LEAVE_APP)
                .source(S_CLOSED).event(E_TRIGGER_COMPLETE).target(S_COMPLETED)
                .and();
    }

    private static void createSMParallelApprovalTransition(StateMachineTransitionConfigurer<TestState, TestEvent> configureTransitions,
        Map<Integer, Long> reviewersMap)
        throws Exception {
        configureTransitions
            .withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_CANCEL).target(S_COMPLETED)
                .action(TransitionAction::cancel)
                .and()
            .withExternal()
                .name(TX_REVIEWER_REJECTS_LEAVE_APP_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_REJECT).target(S_CLOSED)
                .action(TransitionAction::reject)
                .and()
            .withExternal()
                .name(TX_REVIEWER_APPROVES_LEAVE_APPLICATION_IN_PARALLEL_FLOW)
                .source(S_PARALLEL_APPROVAL_FLOW).event(E_APPROVE).target(S_CLOSED)
                .guard(context -> TestGuard.approveInParallel(context, reviewersMap))
                .action(TransitionAction::approveInParallelFlow)
                .and()
            .withExternal()
                .name(TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_PARALLEL_FLOW)
                .source(S_CLOSED).event(E_REQUEST_CHANGES).target(S_PARALLEL_APPROVAL_FLOW)
                .guard(TestGuard::requestChanges)
                .action(TransitionAction::requestChanges)
                .and();
    }

    private static void createSMSerialApprovalTransition(StateMachineTransitionConfigurer<TestState, TestEvent> configureTransitions, Map<Integer, Long> reviewersMap)
        throws Exception {

        configureTransitions.withInternal()
                .name(TX_REVIEWER_FORWARDS_APPLICATION)
                .source(S_SERIAL_APPROVAL_FLOW).event(E_FORWARD)
                .guard(TestGuard::forward)
                .action(TransitionAction::forward);

        configureTransitions.withExternal()
            .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
            .source(S_SERIAL_APPROVAL_FLOW).event(E_ROLL_BACK).target(S_SERIAL_APPROVAL_FLOW)
            .guard(TestGuard::rollBackApproval)
            .action(TransitionAction::rollBackApproval);

        configureTransitions.withExternal()
                .name(TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW)
                .source(S_CLOSED).event(E_ROLL_BACK).target(S_SERIAL_APPROVAL_FLOW)
                .guard(TestGuard::rollBackApproval)
                .action(TransitionAction::rollBackApproval);

        configureTransitions.withExternal()
                .name(TX_USER_CANCELS_LEAVE_APP_IN_SERIAL_FLOW)
                .source(S_SERIAL_APPROVAL_FLOW).event(E_CANCEL).target(S_COMPLETED)
                .action(TransitionAction::cancel)
                .and();

        configureTransitions.withExternal()
                .name(TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_SERIAL_FLOW)
                .source(S_SERIAL_APPROVAL_FLOW).event(E_REQUEST_CHANGES).target(S_SUBMITTED)
                .action(TransitionAction::requestChanges)
                .and();

        configureTransitions.withExternal()
                .name(TX_REVIEWER_REJECTS_LEAVE_APP_IN_SERIAL_FLOW)
                .source(S_SERIAL_APPROVAL_FLOW).event(E_REJECT).target(S_CLOSED)
                .action(TransitionAction::reject)
                .and();

        configureTransitions.withExternal()
                .name(TX_REVIEWER_APPROVES_LEAVE_APP_IN_SERIAL_FLOW)
                .source(S_SERIAL_APPROVAL_FLOW).event(E_APPROVE).target(S_CLOSED)
                .action(TransitionAction::approve)
                .and();
    }

}
