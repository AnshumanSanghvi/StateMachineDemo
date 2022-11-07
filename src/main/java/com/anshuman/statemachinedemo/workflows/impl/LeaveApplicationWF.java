package com.anshuman.statemachinedemo.workflows.impl;

import com.anshuman.statemachinedemo.leaveapp.LeaveAppEvent;
import com.anshuman.statemachinedemo.leaveapp.LeaveAppState;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveApplicationWF {

    private final StateMachineFactory<LeaveAppState, LeaveAppEvent> stateMachineFactory;



}
