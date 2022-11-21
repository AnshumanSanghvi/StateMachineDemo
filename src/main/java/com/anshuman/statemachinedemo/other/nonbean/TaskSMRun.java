package com.anshuman.statemachinedemo.other.nonbean;

import static com.anshuman.statemachinedemo.other.nonbean.Event.APPROVE;
import static com.anshuman.statemachinedemo.other.nonbean.Event.INITIALIZE;
import static com.anshuman.statemachinedemo.other.nonbean.Event.REJECT;
import static com.anshuman.statemachinedemo.other.nonbean.Event.REQUEST_CHANGES;
import static com.anshuman.statemachinedemo.other.nonbean.Event.ROLL_BACK;
import static com.anshuman.statemachinedemo.other.nonbean.Event.SUBMIT;
import static com.anshuman.statemachinedemo.other.nonbean.Event.TRIGGER_REVIEW;

import com.anshuman.statemachinedemo.workflow.util.ReactiveHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

@Component
public class TaskSMRun implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        var stateMachine = TaskStateMachineBuilder.createStateMachine();
        publishEvents(stateMachine, INITIALIZE, SUBMIT, TRIGGER_REVIEW, REQUEST_CHANGES, TRIGGER_REVIEW,
            REQUEST_CHANGES, TRIGGER_REVIEW, APPROVE);

        stateMachine = TaskStateMachineBuilder.createStateMachine();
        publishEvents(stateMachine, INITIALIZE, SUBMIT, TRIGGER_REVIEW, APPROVE, ROLL_BACK, REJECT, ROLL_BACK,
            APPROVE, ROLL_BACK, REJECT);
    }

    private static void publishEvents(StateMachine<State, Event> stateMachine, Event... events) {
        try {
            stateMachine.startReactively().block();
            System.out.println(ReactiveHelper.parseResultToString(stateMachine, events));
            stateMachine.stopReactively().block();
        } catch (Exception ex) {
            // swallow exception
        }
    }

}
