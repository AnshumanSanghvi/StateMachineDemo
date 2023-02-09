package com.anshuman.workflow.statemachine.util;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_APPROVE_BY;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_FORWARDED_BY;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_REQUESTED_CHANGES_BY;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_REQUESTED_CHANGE_COMMENT;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_ROLL_BACK_BY;

import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class EventSendHelper {

    private EventSendHelper() {
        // use class statically
    }

    public static <S, E> Flux<StateMachineEventResult<S, E>> sendEvent(StateMachine<S, E> sm, E event) {
        try {
            var resultFlux = sm.sendEvent(Mono.just(MessageBuilder.withPayload(event).build()));
            log.info("result after {} event: {}", event, EventResultHelper.toResultDTOString(resultFlux));
            return resultFlux;
        } catch (Exception ex) {
            throw new StateMachineException("Could not send event: " + event + " to the state machine with id: " + sm.getId(), ex);
        }
    }

    @SafeVarargs
    public static <S, E> Flux<StateMachineEventResult<S, E>> sendEvents(StateMachine<S, E> sm, E... events) {
        Flux<StateMachineEventResult<S, E>> result = Flux.empty();
        for (E event : events) {
            result = result.mergeWith(sendEvent(sm, event));
        }
        return result;
    }

    public static <S, E> Flux<StateMachineEventResult<S, E>> sendApproveEvent(StateMachine<S, E> sm, E approveEvent, Integer orderNumber, Long reviewerId) {
        checkNull("Approve", orderNumber, reviewerId);
        sm.getExtendedState().getVariables().put(KEY_APPROVE_BY, new Pair<>(orderNumber, reviewerId));
        return sendEvent(sm, approveEvent);
    }

    public static <S, E> Flux<StateMachineEventResult<S, E>> sendRollBackApprovalEvent(StateMachine<S, E> sm, E rollBackEvent, Integer orderNumber,
        Long reviewerId) {
        checkNull("RollBackApproval", orderNumber, reviewerId);
        sm.getExtendedState().getVariables().put(KEY_ROLL_BACK_BY, new Pair<>(orderNumber, reviewerId));
        return sendEvent(sm, rollBackEvent);
    }

    public static <S, E> Flux<StateMachineEventResult<S, E>> sendRequestChangesEvent(StateMachine<S, E> sm, E requestChangesEvent, Integer orderNumber,
        Long reviewerId, String comment) {
        checkNull("RequestChanges", orderNumber, reviewerId);
        var map = sm.getExtendedState().getVariables();
        map.put(KEY_REQUESTED_CHANGES_BY, new Pair<>(orderNumber, reviewerId));
        map.put(KEY_REQUESTED_CHANGE_COMMENT, comment);
        return sendEvent(sm, requestChangesEvent);
    }

    public static <S, E> Flux<StateMachineEventResult<S, E>> sendForwardEvent(StateMachine<S, E> sm, E forwardEvent, Integer orderNumber, Long reviewerId) {
        checkNull("Forward", orderNumber, reviewerId);
        var map = sm.getExtendedState().getVariables();
        map.put(KEY_FORWARDED_BY, new Pair<>(orderNumber, reviewerId));
        return sendEvent(sm, forwardEvent);
    }

    private static void checkNull(String eventName, Integer orderNumber, Long reviewerId) {
        if (reviewerId == null)
            throw new StateMachineException(new NullPointerException(eventName + " Event - reviewerId cannot be null"));
        if (orderNumber == null)
            throw new StateMachineException(new NullPointerException(eventName + " Event - orderNumber cannot be null"));
    }
}
