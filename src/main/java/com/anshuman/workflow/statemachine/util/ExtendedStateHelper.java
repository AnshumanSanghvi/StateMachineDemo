package com.anshuman.workflow.statemachine.util;

import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.event.TestEvent;
import com.anshuman.workflow.statemachine.state.TestState;
import java.util.Collections;
import java.util.Map;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;

public class ExtendedStateHelper {

    private ExtendedStateHelper() {
        //use statically
    }

    public static Boolean getBoolean(StateContext<TestState, TestEvent> context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(StateContext<TestState, TestEvent> context, String key, boolean defaultVal) {
        return getBoolean(context.getExtendedState(), key, defaultVal);
    }

    public static Boolean getBoolean(ExtendedState extendedState, String key, boolean defaultVal) {
        return (Boolean) extendedState.getVariables().getOrDefault(key, defaultVal);
    }

    /**
     * Get the integer value for the given key from the extended state map. Default to 0 if not found.
     * @param context state context
     * @param key key for the map
     * @return integer value associated with the key
     */
    public static Integer getInt(StateContext<TestState, TestEvent> context, String key) {
        return getInt(context, key, 0);
    }

    public static Integer getInt(StateContext<TestState, TestEvent> context, String key, int defaultInt) {
        return getInt(context.getExtendedState(), key, defaultInt);
    }

    public static Integer getInt(ExtendedState extendedState, String key, int defaultInt) {
        return (Integer) extendedState.getVariables().getOrDefault(key, defaultInt);
    }

    public static Long getLong(StateContext<TestState, TestEvent> context, String key) {
        return getLong(context.getExtendedState(), key, 0L);
    }

    public static Long getLong(ExtendedState extendedState, String key, Long defaultVal) {
        return (Long) extendedState.getVariables().getOrDefault(key, defaultVal);
    }

    /**
     * Get the string value for the given key from the extended state map. Default to empty string if not found.
     * @param context state context
     * @param key key for the map
     * @return string value associated with the key
     */
    public static String getString(StateContext<TestState, TestEvent> context, String key) {
        return getString(context, key, "");
    }

    public static String getString(StateContext<TestState, TestEvent> context, String key, String defaultStr) {
        return getString(context.getExtendedState(), key, defaultStr);
    }

    public static String getString(ExtendedState extendedState, String key, String defaultStr) {
        return (String) extendedState.getVariables().getOrDefault(key, defaultStr);
    }

    public static <S, E> S getStateId(StateContext<S, E> context) {
        return context.getStateMachine().getState().getId();
    }

    public static Pair<Integer, Long> getPair(StateContext<TestState, TestEvent> context, String key) {
        return getPair(context.getExtendedState(), key, null);
    }

    @SuppressWarnings("unchecked")
    public static Pair<Integer, Long> getPair(ExtendedState extendedState, String key, Pair<Integer, Long> defaultPair) {
        return (Pair<Integer, Long>) extendedState.getVariables().getOrDefault(key, defaultPair);
    }

    @SuppressWarnings("unchecked")
    public static <V> V getValue(StateContext<TestState, TestEvent> context, String key, V defaultVal) {
        var map = context.getExtendedState().getVariables();
        return (V) map.getOrDefault(key, defaultVal);
    }

    public static <K, V> Map<K, V> getMap(StateContext<TestState, TestEvent> context, String key) {
        return getMap(context.getExtendedState(), key, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getMap(ExtendedState extendedState, String key, Map<K, V> defaultMap) {
        var map = extendedState.getVariables();
        return (Map<K, V>) map.getOrDefault(key, defaultMap);
    }
}
