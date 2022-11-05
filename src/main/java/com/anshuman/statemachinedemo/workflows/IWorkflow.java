package com.anshuman.statemachinedemo.workflows;

import com.anshuman.statemachinedemo.config.StateMachineConfig.AppEvent;

public interface IWorkflow {

    Object getState(Long workflowInstanceId, AppEvent event);

    Object setState();

}
