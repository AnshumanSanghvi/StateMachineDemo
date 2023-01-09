package com.anshuman.statemachinedemo.workflow.data.dto;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.region.Region;

@Getter
@Setter
public class EventResultDTO<S, E> {

    private String region;
    private boolean isSubMachine;
    private E event;
    private ResultType resultType;
    private S currentState;

    public static final Predicate<EventResultDTO> accepted = result -> result.getResultType().equals(ResultType.ACCEPTED);

    public EventResultDTO(StateMachineEventResult<S, E> result) {
        Region<S, E> stateMachineRegion = result.getRegion();
        this.setRegion(stateMachineRegion.getUuid().toString() + " (" + stateMachineRegion.getId() + ")");
        this.isSubMachine = stateMachineRegion.getState().isSubmachineState();
        this.setEvent(result.getMessage().getPayload());
        this.setResultType(result.getResultType());
        this.setCurrentState(stateMachineRegion.getState().getId());
    }

    @Override
    public String toString() {
        return "EventResultDTO[" +
            "stateMachine: " + this.region +
            ", isSubMachine: " + this.isSubMachine +
            ", event: " + this.event +
            ", currentState: " + this.currentState +
            ", resultType: " + this.resultType +
            "]";
    }
}
