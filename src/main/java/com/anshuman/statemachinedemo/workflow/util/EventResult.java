package com.anshuman.statemachinedemo.workflow.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;

@Getter
@Setter
@ToString
public class EventResult<S, E> {

    private String region;
    private E event;
    private ResultType resultType;
    private boolean isComplete;

    public EventResult(StateMachineEventResult<S, E> result) {
        this.setEvent(result.getMessage().getPayload());
        this.setRegion(result.getRegion().getId());
        this.setResultType(result.getResultType());
        this.isComplete = result.complete() != null;
    }
}
