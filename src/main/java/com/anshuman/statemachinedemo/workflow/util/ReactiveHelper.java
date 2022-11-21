package com.anshuman.statemachinedemo.workflow.util;

import com.anshuman.statemachinedemo.workflow.exception.StateMachineException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @SafeVarargs
    public static <S, E> Flux<EventResult<S, E>> parseResult(StateMachine<S, E> stateMachine, E... events) {
        return stateMachine.sendEvents(ReactiveHelper.toMessageFlux(events))
            .doOnError(ex -> log.error("Exception encountered: {}", ex.getMessage(), ex))
            .onErrorStop()
            .map(EventResult::new);
    }

    @SafeVarargs
    public static <S, E> boolean eventsSentSuccessfully(StateMachine<S, E> stateMachine, E... events) {
        return Optional.ofNullable(parseResultToList(stateMachine, events))
            .stream()
            .flatMap(Collection::stream)
            .peek(eventResult -> log.trace("EventResult: {}", eventResult))
            .map(EventResult::getResultType)
            .allMatch(resultType -> resultType.equals(ResultType.ACCEPTED));
    }

    @SafeVarargs
    public static <S, E> String parseResultToString(StateMachine<S, E> stateMachine, E... events) {
        return "[" + Optional
            .ofNullable(parseResultToList(stateMachine, events))
            .or(() -> Optional.of(Collections.emptyList()))
            .stream()
            .flatMap(Collection::stream)
            .map(EventResult::toString)
            .collect(Collectors.joining(",\n")) + "]";
    }

    @SafeVarargs
    private static <S, E> List<EventResult<S, E>> parseResultToList(StateMachine<S, E> stateMachine, E... events) {
        List<EventResult<S, E>> eventResults = ReactiveHelper
            .parseResult(stateMachine, events)
            .collectList()
            .block();
        log.trace("Parsing StateMachine eventresults to list: {}", eventResults);
        return eventResults;
    }

    @SafeVarargs
    public static <S, E> List<EventResult<S, E>> stateMachineHandler(StateMachine<S, E> stateMachine, E... events) {
        try {
            stateMachine.startReactively().block();
            var results = ReactiveHelper.parseResultToList(stateMachine, events);
            stateMachine.stopReactively().block();
            return results;
        } catch (Exception ex) {
            throw new StateMachineException(ex);
        }
    }

}
