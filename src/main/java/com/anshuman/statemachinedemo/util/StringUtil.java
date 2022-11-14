package com.anshuman.statemachinedemo.util;

import static java.util.stream.Collectors.joining;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

@UtilityClass
public class StringUtil {

    public static <K, V> String state(State<K, V> state) {
        return Optional.ofNullable(state)
            .map(State::getId)
            .map(String::valueOf)
            .orElse("");
    }

    public static <K, V> String stageFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getStage()))
            .flatMap(stage -> Optional
                .of(stage.name())
                .map(st -> " Stage: " + st))
            .orElse("");
    }

    public static <K, V> String sourceStateFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getSource()))
            .map(st -> " | Source: " + st.getId())
            .orElse("");
    }

    public static <K, V> String eventFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getEvent()))
            .map(e -> " | Event: " + e)
            .orElse("");
    }

    public static <K, V> String targetStateFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getTarget()))
            .map(st -> " | Target: " + st.getId())
            .orElse("");
    }

    public static <K, V> String sourceStateFromTransition(Transition<K, V> transition) {
        return Optional
            .ofNullable(transition)
            .map(Transition::getSource)
            .map(t -> " from: " + t.getId())
            .orElse("");
    }

    public static <K, V> String targetStateFromTransition(Transition<K, V> transition) {
        return Optional
            .ofNullable(transition)
            .map(Transition::getTarget)
            .map(t -> " to: " + t.getId())
            .orElse("");
    }

    public static String messageHeaders(MessageHeaders headers) {
        return Optional.ofNullable(headers)
            .filter(Predicate.not(MessageHeaders::isEmpty))
            .map(MessageHeaders::entrySet)
            .map(StringUtil::entrySet)
            .orElse("");
    }

    private static String object(Object object) {
        return Optional
            .ofNullable(object)
            .map(Object::toString)
            .orElse("");
    }

    private static <K, V> String entrySet(Set<Entry<K, V>> entrySet) {
        if (entrySet == null)
            return "";
        if (entrySet.isEmpty())
            return "";
        return entrySet
            .stream()
            .map(entry -> "{key: " + object(entry.getKey()) + ", value: " + object(entry.getValue()) + "}")
            .collect(joining(", "));
    }

    public static <S, E> String extendedStateFromContext(StateContext<S, E> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getExtendedState()))
            .flatMap(es -> Optional.ofNullable(es.getVariables()))
            .filter(Predicate.not(Map::isEmpty))
            .map(Map::entrySet)
            .map(StringUtil::entrySet)
            .filter(Predicate.not(String::isEmpty))
            .map(str -> " | ExtendedState: " + str)
            .orElse("");
    }

    public static <S, E> String transition(Transition<S, E> transition) {
        if (transition == null)
            return "";

        String name = Optional.ofNullable(transition.getName())
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "name: " + s)
            .orElse("");

        String sourceState = Optional.ofNullable(transition.getSource())
            .map(State::getId)
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "source state: " + s)
            .orElse("");

        String targetState =  Optional.ofNullable(transition.getTarget())
            .map(State::getId)
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "target state: " + s)
            .orElse("");

        String actions  = Optional.ofNullable(transition.getActions())
            .map(list -> list.stream().map(Object::toString).collect(joining(", ")))
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "actions: " + s)
            .orElse("");

        String kind = Optional.ofNullable(transition.getKind())
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "kind: " + s)
            .orElse("");

        String guard = Optional.ofNullable(transition.getGuard())
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "guard: " + s)
            .orElse("");

        String trigger = Optional.ofNullable(transition.getTrigger())
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "trigger: " + s)
            .orElse("");
        
        return Stream.of(name, sourceState, targetState, kind, guard, actions, trigger)
            .filter(Predicate.not(String::isEmpty).and(Predicate.not(String::isBlank)))
            .collect(joining(", "));
            
    }

}
