package com.anshuman.workflow.statemachine.util;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.*;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getInt;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getString;
import static java.util.stream.Collectors.toMap;

import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.statemachine.data.Pair;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;

@Slf4j
public class WFPropsToSMExtStateHelper {

    private WFPropsToSMExtStateHelper() {
        // use class statically.
    }

    public static <S, E> void setExtendedStateProperties(StateMachine<S, E> stateMachine, WorkflowProperties properties, List<Pair<Integer, Long>> reviewers) {

        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(pairListToMap(reviewers));

        Optional.ofNullable(stateMachine)
            .map(StateMachine::getExtendedState)
            .flatMap(exs -> Optional.ofNullable(exs.getVariables()))
            .ifPresent(map -> {
                map.put(KEY_ROLL_BACK_MAX, properties.getMaximumRollbackApprovalThreshold());
                map.put(KEY_ROLL_BACK_COUNT, 0);
                map.put(KEY_RETURN_COUNT, 0);
                map.put(KEY_CLOSED_STATE_TYPE, "");
                map.put(KEY_APPROVAL_FLOW_TYPE, properties.isHasParallelApproval() ? VAL_PARALLEL : VAL_SERIAL);
                map.put(KEY_REVIEWERS_COUNT, reviewerMap.size());
                map.put(KEY_MAX_CHANGE_REQUESTS, properties.getMaximumChangeRequestThreshold());
                map.put(KEY_REVIEWERS_MAP, reviewerMap);
                map.put(KEY_FORWARDED_MAP, reviewerMap.entrySet().stream()
                    .collect(toMap(Entry::getKey,
                        entry -> new Pair<>(entry.getValue(), false))));

                ExtendedState extendedState = stateMachine.getExtendedState();
                log.trace("Setting extended state- rollbackCount: {}, returnCount: {}, closedState: {}, reviewersCount: {}, "
                        + "reviewersList: {}",
                    getInt(extendedState, KEY_ROLL_BACK_COUNT, 0),
                    getInt(extendedState, KEY_RETURN_COUNT, 0),
                    getString(extendedState, KEY_CLOSED_STATE_TYPE, ""),
                    getInt(extendedState, KEY_REVIEWERS_COUNT, 0),
                    map.get(KEY_REVIEWERS_MAP));
            });
    }

    private static <K, V> Map<K, V> pairListToMap(List<Pair<K, V>> pairList) {
        return pairList
            .stream()
            .collect(toMap(Pair::getFirst,Pair::getSecond));
    }

}
