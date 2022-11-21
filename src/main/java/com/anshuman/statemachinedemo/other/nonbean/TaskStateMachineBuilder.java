package com.anshuman.statemachinedemo.other.nonbean;

import static com.anshuman.statemachinedemo.other.nonbean.Event.APPROVE;
import static com.anshuman.statemachinedemo.other.nonbean.Event.CANCEL;
import static com.anshuman.statemachinedemo.other.nonbean.Event.INITIALIZE;
import static com.anshuman.statemachinedemo.other.nonbean.Event.REJECT;
import static com.anshuman.statemachinedemo.other.nonbean.Event.REQUEST_CHANGES;
import static com.anshuman.statemachinedemo.other.nonbean.Event.ROLL_BACK;
import static com.anshuman.statemachinedemo.other.nonbean.Event.SUBMIT;
import static com.anshuman.statemachinedemo.other.nonbean.Event.TRIGGER_COMPLETE;
import static com.anshuman.statemachinedemo.other.nonbean.Event.TRIGGER_REVIEW;
import static com.anshuman.statemachinedemo.other.nonbean.State.CLOSED;
import static com.anshuman.statemachinedemo.other.nonbean.State.COMPLETED;
import static com.anshuman.statemachinedemo.other.nonbean.State.CREATED;
import static com.anshuman.statemachinedemo.other.nonbean.State.INITIAL;
import static com.anshuman.statemachinedemo.other.nonbean.State.SUBMITTED;
import static com.anshuman.statemachinedemo.other.nonbean.State.UNDER_PROCESS;

import com.anshuman.statemachinedemo.other.StateMachineInterceptor;
import com.anshuman.statemachinedemo.workflow.config.StateMachineListener;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;

// test ways to have dynamic state machine building, e.g reviewer flow per number of total reviewers
// test ☑️guards, ☑️actions, pseudo states, junctions, sub machines, regions ☑️ (execution is parallel)
// test external, ☑️internal and local transitions
@Slf4j
public class TaskStateMachineBuilder {

    public static StateMachine<State, Event> createStateMachine() throws Exception {
        Builder<State, Event> builder = StateMachineBuilder.builder();

        createSMConfig(builder);

        createSMStateConfig(builder);

        createSMTransitionConfig(builder);

        StateMachine<State, Event> stateMachine = builder.build();

        stateMachine
            .getStateMachineAccessor()
            .withAllRegions()
            .forEach(region -> region.addStateMachineInterceptor(new StateMachineInterceptor<>()));

        return stateMachine;
    }

    private static void createSMConfig(Builder<State, Event> builder) throws Exception {
        builder
            .configureConfiguration()
            .withConfiguration()
            .machineId("testStateMachine")
            .listener(new StateMachineListener<>());
    }

    private static void createSMStateConfig(Builder<State, Event> builder) throws Exception {
        builder
            .configureStates()
            .withStates()
            .initial(INITIAL)
            .end(COMPLETED)
            .states(Set.of(INITIAL, CREATED, SUBMITTED, UNDER_PROCESS, CLOSED, COMPLETED));
    }


    private static void createSMTransitionConfig(Builder<State, Event> builder) throws Exception {
        builder
            .configureTransitions()
                .withExternal()
                    .name("UserCreatesTheLeaveApplication")
                    .source(INITIAL)
                    .event(INITIALIZE)
                    .target(CREATED)
                    .action(MyActions::initializeAction)
                    .and()
                .withExternal()
                    .name("UserSubmitsTheCreatedLeaveApplication")
                    .source(CREATED)
                    .event(SUBMIT)
                    .target(SUBMITTED)
                    .and()
                .withExternal()
                    .name("SystemTriggersTheSubmittedLeaveApplication")
                    .source(SUBMITTED)
                    .event(TRIGGER_REVIEW)
                    .target(UNDER_PROCESS)
                    .guard(MyGuards::applicationReturnGuard)
                    .and()
                .withExternal()
                    .name("ReviewerRequestsChangesInTheLeaveApplicationUnderReview")
                    .source(UNDER_PROCESS)
                    .event(REQUEST_CHANGES)
                    .target(SUBMITTED)
                    .action(MyActions::requestChangesAction)
                    .and()
                .withExternal()
                    .name("UserCancelsTheLeaveApplicationUnderReview")
                    .source(UNDER_PROCESS)
                    .event(CANCEL)
                    .target(CLOSED)
                    .action(MyActions::cancelAction)
                    .and()
                .withExternal()
                    .name("ReviewerApprovesTheLeaveApplicationUnderReview")
                    .source(UNDER_PROCESS)
                    .event(APPROVE)
                    .target(CLOSED)
                    .action(MyActions::approveAction)
                    .and()
                .withExternal()
                    .name("ReviewerRejectsTheLeaveApplicationUnderReview")
                    .source(UNDER_PROCESS)
                    .event(REJECT)
                    .target(CLOSED)
                    .action(MyActions::rejectAction)
                    .and()
                .withExternal()
                    .name("ReviewerRollsBackTheLeaveApplicationUnderReview")
                    .source(CLOSED)
                    .event(ROLL_BACK)
                    .target(UNDER_PROCESS)
                    .action(MyActions::rollBackAction)
                    .guard(MyGuards::applicationRollBackGuard)
                    .and()
                .withExternal()
                    .name("SystemCompletesTheLeaveApplication")
                    .source(CLOSED)
                    .event(TRIGGER_COMPLETE)
                    .target(COMPLETED);
    }

}
