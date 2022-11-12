package com.anshuman.statemachinedemo.workflow.service;

import com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.persist.ContextEntity;
import com.anshuman.statemachinedemo.workflow.persist.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveApplicationWFService {

    private final DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, ContextEntity<LeaveAppState, LeaveAppEvent, Long>> stateMachineAdapter;

    public void createStateMachine(Boolean isParallel) {
        var stateMachine = stateMachineAdapter.create();
        ExtendedState extendedState = stateMachine.getExtendedState();
        Map<Object, Object> map = extendedState.getVariables();
        Optional.ofNullable(isParallel).ifPresent(value -> map.put(LeaveAppConstants.IS_PARALLEL, value));
    }


}
