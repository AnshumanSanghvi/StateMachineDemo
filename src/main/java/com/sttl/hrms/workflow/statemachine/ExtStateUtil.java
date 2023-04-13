package com.sttl.hrms.workflow.statemachine;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;

import java.util.Optional;

class ExtStateUtil {
    static String getStateId(StateContext<String, String> context) {
        return context.getStateMachine().getState().getId();
    }

    static <T> T get(StateContext<String, String> context, String key, Class<T> clazz, T defaultVal) {
        return get(context.getExtendedState(), key, clazz, defaultVal);
    }

    static <T> T get(ExtendedState extendedState, String key, Class<T> clazz, T defaultVal) {
        return Optional.ofNullable(extendedState.get(key, clazz)).orElse(defaultVal);
    }
}
