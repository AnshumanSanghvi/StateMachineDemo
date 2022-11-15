package com.anshuman.statemachinedemo.workflow.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.region.Region;

@Getter
@Setter
public class EventResult<S, E> {

    private Region<S, E> region;
    private E event;
    private ResultType resultType;

    public EventResult(StateMachineEventResult<S, E> result) {
        this.setEvent(result.getMessage().getPayload());
        this.setRegion(result.getRegion());
        this.setResultType(result.getResultType());
    }
}
