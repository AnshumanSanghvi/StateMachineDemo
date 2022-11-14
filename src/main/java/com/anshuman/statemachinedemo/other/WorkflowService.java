package com.anshuman.statemachinedemo.other;

import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowService<S, E> {

    private final DefaultStateMachineService<S, E> stateMachineService;

    public void acquireStateMachine(String stateMachineId) {
        StateMachine<S, E> stateMachine = stateMachineService.acquireStateMachine(stateMachineId, false);
    }

    public void releaseStateMachine(String stateMachineId) {
        stateMachineService.releaseStateMachine(stateMachineId, true);
    }


    @PreDestroy
    public void destroy() throws Exception {
        stateMachineService.destroy();
    }
}
