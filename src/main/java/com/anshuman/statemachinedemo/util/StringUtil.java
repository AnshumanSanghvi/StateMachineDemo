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
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

@UtilityClass
public class StringUtil {

    public static <S, E> String state(State<S, E> state) {
        return Optional.ofNullable(state)
            .map(State::getId)
            .map(String::valueOf)
            .orElse("null");
    }

    public static <S, E> String stageFromContext(StateContext<S, E> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getStage()))
            .flatMap(stage -> Optional.of(stage.name()))
            .orElse("null");
    }

    public static <S, E> String sourceStateFromContext(StateContext<S, E> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getSource()))
            .map(State::getId)
            .map(S::toString)
            .orElse("null");
    }

    public static <S, E> String eventFromContext(StateContext<S, E> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .map(StringUtil::event)
            .orElse("null");
    }

    public static <S, E> String event(E event) {
        return Optional.ofNullable(event)
            .map(E::toString)
            .orElse("null");
    }

    public static <S, E> String targetStateFromContext(StateContext<S, E> stateContext) {
        return Optional
            .ofNullable(stateContext)
            .flatMap(sc -> Optional.ofNullable(sc.getTarget()))
            .map(State::getId)
            .map(S::toString)
            .orElse("null");
    }

    public static <S, E> String sourceStateFromTransition(Transition<S, E> transition) {
        return Optional
            .ofNullable(transition)
            .map(Transition::getSource)
            .map(State::getId)
            .map(S::toString)
            .orElse("null");
    }

    public static <S, E> String targetStateFromTransition(Transition<S, E> transition) {
        return Optional
            .ofNullable(transition)
            .map(Transition::getTarget)
            .map(State::getId)
            .map(S::toString)
            .orElse("null");
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
            .orElse("null");
    }

    public static String extendedState(ExtendedState extendedState) {
        return Optional.ofNullable(extendedState)
            .map(ExtendedState::getVariables)
            .filter(Predicate.not(Map::isEmpty))
            .map(Map::entrySet)
            .map(StringUtil::entrySet)
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

    private static <S, E> String entrySet(Set<Entry<S, E>> entrySet) {
        if (entrySet == null)
            return "";
        if (entrySet.isEmpty())
            return "";
        return entrySet
            .stream()
            .map(entry -> "{key: " + object(entry.getKey()) + ", value: " + object(entry.getValue()) + "}")
            .collect(joining(", "));
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
            .map(s -> "{source state: " + s + (transition.getSource().isSubmachineState() ? ", isSubMachine: true":"") + "}")
            .orElse("");

        String targetState = Optional.ofNullable(transition.getTarget())
            .map(State::getId)
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "{target state: " + s + (transition.getTarget().isSubmachineState() ? ", isSubMachine: true":"") + "}")
            .orElse("");

        String event = Optional.ofNullable(transition.getTrigger())
            .flatMap(trigger -> Optional.ofNullable(trigger.getEvent()))
            .map(e -> "event: " + e)
            .orElse("");

        String kind = Optional.ofNullable(transition.getKind())
            .map(Object::toString)
            .filter(Predicate.not(String::isEmpty))
            .map(s -> "kind: " + s)
            .orElse("");

        return Stream.of(name, sourceState, event, targetState, kind) //, guard, actions, trigger)
            .filter(Predicate.not(String::isEmpty).and(Predicate.not(String::isBlank)))
            .collect(joining(", "));

    }

    public static <S, E> String stateMachine(StateMachine<S, E> stateMachine, boolean detailed) {
        if (stateMachine == null) {
            return "";
        }

        String stateStr = state(stateMachine.getState());
        String idStr = stateMachine.getId();
        String output = "StateMachine[id: " + idStr + ", currentState: " + stateStr;

        if(detailed) {
            String extendedStateStr = extendedState(stateMachine.getExtendedState());
            String uuidStr = stateMachine.getUuid().toString();
            String allStatesStr = stateMachine.getStates().stream().map(StringUtil::state).collect(joining(", "));
            String initialStateStr = StringUtil.state(stateMachine.getInitialState());
            String transitionsStr = stateMachine.getTransitions().stream().map(StringUtil::transition).collect(joining(", "));
            output+= ", uuid: " + uuidStr +
                ", extendedState: " + extendedStateStr +
                ", initialState: " + initialStateStr +
                " allStates: {" + allStatesStr + "}" +
                ", transitions: {" + transitionsStr + "}";
        }

        return output + "]";
    }

}
