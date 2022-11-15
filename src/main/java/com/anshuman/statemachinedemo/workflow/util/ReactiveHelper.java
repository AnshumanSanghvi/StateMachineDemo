package com.anshuman.statemachinedemo.workflow.util;

import java.util.Arrays;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@UtilityClass
@Slf4j
public class ReactiveHelper {

    public static <E> Mono<Message<E>> toMessageMono(E event) {
        return Mono.just(WFHelper.toMessage(event));
    }

    @SafeVarargs
    public static <E> Flux<Message<E>> toMessageFlux(E... events) {
        return Flux.fromStream(Arrays.stream(events).map(WFHelper::toMessage));
    }

    public static <S, E> Mono<S> getCurrentState(StateMachine<S, E> stateMachine) {
        return Mono.defer(() -> Mono.justOrEmpty(stateMachine.getState().getId()));
    }

    @SafeVarargs
    public static <S, E> Flux<EventResult<S, E>> parseResult(StateMachine<S, E> stateMachine, E... events) {
        return stateMachine.sendEvents(ReactiveHelper.toMessageFlux(events)).map(EventResult::new);
    }

    @SafeVarargs
    public static <S, E> boolean eventSentSuccessfully(StateMachine<S, E> stateMachine, E... events) {
        return ReactiveHelper
            .parseResult(stateMachine, events)
            .toStream()
            .peek(eventResult -> log.trace("EventResult: {}", eventResult))
            .map(EventResult::getResultType)
            .allMatch(resultType -> resultType.equals(ResultType.ACCEPTED));
    }

}
