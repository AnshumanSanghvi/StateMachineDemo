package com.anshuman.workflow.statemachine.util;

import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_APPROVE_BY;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_FORWARDED_BY;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_REQUESTED_CHANGES_BY;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_REQUESTED_CHANGE_COMMENT;
import static com.anshuman.workflow.statemachine.data.constant.TestConstant.KEY_ROLL_BACK_BY;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_APPROVE;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_FORWARD;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_REQUEST_CHANGES;
import static com.anshuman.workflow.statemachine.event.TestEvent.E_ROLL_BACK;

import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.event.TestEvent;
import com.anshuman.workflow.statemachine.state.TestState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

@Slf4j
public class EventSendHelper {

    private EventSendHelper() {
        // use class statically
    }

    public static void sendEvent(StateMachine<TestState, TestEvent> sm, TestEvent event) {
        try {
            log.info("result after {} event: {}", event,
                StringUtil.stateMachineEventResult(sm.sendEvent(toMonoMsg(event))));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void sendEvents(StateMachine<TestState, TestEvent> sm, TestEvent... events) {
        for (TestEvent event : events) {
            sendEvent(sm, event);
        }
    }

    public static void sendApproveEvent(StateMachine<TestState, TestEvent> sm, Integer orderNumber, Long reviewerId) {
        checkNull("Approve", orderNumber, reviewerId);
        sm.getExtendedState().getVariables().put(KEY_APPROVE_BY, new Pair<>(orderNumber, reviewerId));
        sendEvent(sm, E_APPROVE);
    }

    public static void sendRollBackApprovalEvent(StateMachine<TestState, TestEvent> sm, Integer orderNumber, Long reviewerId) {
        checkNull("RollBackApproval", orderNumber, reviewerId);
        sm.getExtendedState().getVariables().put(KEY_ROLL_BACK_BY, new Pair<>(orderNumber, reviewerId));
        sendEvent(sm, E_ROLL_BACK);
    }

    public static void sendRequestChangesEvent(StateMachine<TestState, TestEvent> sm, Integer orderNumber, Long reviewerId, String comment) {
        checkNull("RequestChanges", orderNumber, reviewerId);
        var map = sm.getExtendedState().getVariables();
        map.put(KEY_REQUESTED_CHANGES_BY, new Pair<>(orderNumber, reviewerId));
        map.put(KEY_REQUESTED_CHANGE_COMMENT, comment);
        sendEvent(sm, E_REQUEST_CHANGES);
    }

    public static void sendForwardEvent(StateMachine<TestState, TestEvent> sm, Integer orderNumber, Long reviewerId) {
        checkNull("Forward", orderNumber, reviewerId);
        var map = sm.getExtendedState().getVariables();
        map.put(KEY_FORWARDED_BY, new Pair<>(orderNumber, reviewerId));
        sendEvent(sm, E_FORWARD);
    }



    public static Mono<Message<TestEvent>> toMonoMsg(TestEvent event) {
        return Mono.just(MessageBuilder.withPayload(event).build());
    }

    private static void checkNull(String eventName, Integer orderNumber, Long reviewerId) {
        if (reviewerId == null)
            throw new WorkflowException(new NullPointerException(eventName + " TestEvent - reviewerId cannot be null"));
        if (orderNumber == null)
            throw new WorkflowException(new NullPointerException(eventName + " TestEvent - orderNumber cannot be null"));
    }
}
