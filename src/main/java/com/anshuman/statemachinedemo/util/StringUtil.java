package com.anshuman.statemachinedemo.util;

import java.util.Optional;
import lombok.experimental.UtilityClass;
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
            .ofNullable(stateContext.getStage())
            .flatMap(stage -> Optional
                .of(stage.name())
                .map(st -> " Stage: " + st)
            ).orElse("");
    }

    public static <K, V> String sourceStateFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext.getSource())
            .map(st -> " | Source: " + st.getId())
            .orElse("");
    }

    public static <K, V> String eventFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext.getEvent())
            .map(e ->" | Event: " + e)
            .orElse("");
    }

    public static <K, V> String targetStateFromContext(StateContext<K, V> stateContext) {
        return Optional
            .ofNullable(stateContext.getTarget())
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

}
