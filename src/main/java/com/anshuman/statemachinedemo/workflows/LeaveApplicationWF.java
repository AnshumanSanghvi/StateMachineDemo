package com.anshuman.statemachinedemo.workflows;

import com.anshuman.statemachinedemo.config.StateMachineConfig.AppEvent;
import com.anshuman.statemachinedemo.config.StateMachineConfig.AppState;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveApplicationWF {

    private final StateMachineFactory<AppState, AppEvent> stateMachineFactory;



}
