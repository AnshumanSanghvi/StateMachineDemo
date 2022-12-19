package com.anshuman.statemachinedemo.workflow.util;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.region.Region;

@Getter
@Setter
public class EventResult<S, E> {

    private String region;
    private boolean isSubMachine;
    private E event;
    private ResultType resultType;
    private S currentState;

    public static final Predicate<EventResult> accepted = result -> result.getResultType().equals(ResultType.ACCEPTED);

    public EventResult(StateMachineEventResult<S, E> result) {
        Region<S, E> region = result.getRegion();
        this.setRegion(region.getUuid().toString() + " (" + region.getId() + ")");
        this.isSubMachine = region.getState().isSubmachineState();
        this.setEvent(result.getMessage().getPayload());
        this.setResultType(result.getResultType());
        this.setCurrentState(region.getState().getId());
    }

    @Override
    public String toString() {
        return "EventResult[" +
            "stateMachine: " + this.region +
            ", isSubMachine: " + this.isSubMachine +
            ", event: " + this.event +
            ", currentState: " + this.currentState +
            ", resultType: " + this.resultType +
            "]";
    }
}
