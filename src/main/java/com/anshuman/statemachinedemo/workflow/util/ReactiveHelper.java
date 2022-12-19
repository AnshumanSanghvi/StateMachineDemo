package com.anshuman.statemachinedemo.workflow.util;

import com.anshuman.statemachinedemo.workflow.exception.StateMachineException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
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
    private static <S, E> List<EventResult<S, E>> parseResultToList(StateMachine<S, E> stateMachine, E... events) {
        List<EventResult<S, E>> eventResults = ReactiveHelper
            .parseResult(stateMachine, events)
            .collectList()
            .block();
        log.trace("Parsing StateMachine eventresults to list: {}", eventResults);
        return eventResults;
    }

    public static <S, E> boolean parseResultToBool(List<EventResult<S, E>> results) {
        return Optional.ofNullable(results)
            .filter(Predicate.not(List::isEmpty)) // return false if there are no results
            .stream()
            .flatMap(List::stream)
            .peek(result ->log.trace("event result: {}", result)) // log all event results
            .filter(Predicate.not(EventResult.accepted))
            .peek(result -> log.warn("event not accepted: {}", result))
            .findAny() // if any event with error is found
            .isEmpty(); // then return false
    }

    @SafeVarargs
    public static <S, E> boolean parseResultToBool(StateMachine<S, E> stateMachine, E... events) {
        return parseResultToBool(parseResultToList(stateMachine, events));
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
