package com.sttl.hrms.workflow.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.region.Region;

import java.util.Optional;
import java.util.function.Predicate;

import static com.sttl.hrms.workflow.statemachine.SMConstants.*;

@Getter
@Setter
public class EventResultDto {

    private String region;
    private boolean isSubMachine;
    private String event;
    private ResultType resultType;
    private String currentState;
    private boolean isComplete;
    private Integer order;
    private Long actionBy;
    private String comment;

    public static final Predicate<EventResultDto> accepted = result -> result.getResultType()
            .equals(ResultType.ACCEPTED);

    public EventResultDto(StateMachineEventResult<String, String> result) {
        Region<String, String> stateMachineRegion = result.getRegion();
        this.setRegion(stateMachineRegion.getUuid().toString() + " (" + stateMachineRegion.getId() + ")");
        this.isSubMachine = stateMachineRegion.getState().isSubmachineState();
        this.setEvent(result.getMessage().getPayload());
        this.setResultType(result.getResultType());
        this.setCurrentState(stateMachineRegion.getState().getId());
        this.setComplete(stateMachineRegion.isComplete());

        MessageHeaders headers = result.getMessage().getHeaders();
        Optional.ofNullable(headers.get(MSG_KEY_ORDER_NO, Integer.class)).ifPresent(this::setOrder);
        Optional.ofNullable(headers.get(MSG_KEY_ACTION_BY, Long.class)).ifPresent(this::setActionBy);
        Optional.ofNullable(headers.get(MSG_KEY_COMMENT, String.class)).filter(Predicate.not(String::isBlank))
                .ifPresent(this::setComment);
    }

    @Override
    public String toString() {
        String submachine = this.isSubMachine ? ", isSubMachine: true" : "";
        String complete = this.isComplete ? ", isComplete: true" : "";
        String order = (this.order != null) ? ", order: " + this.order : "";
        String actionBy = (this.actionBy != null) ? ", actionBy: " + this.actionBy : "";
        String comment = (this.comment != null) ? ", comment: " + this.comment : "";
        return "EventResultDto[" +
                "stateMachine: " + this.region +
                ", currentState: " + this.currentState +
                ", event: " + this.event +
                ", resultType: " + this.resultType +
                order +
                actionBy +
                comment +
                submachine +
                complete +
                "]";
    }
}
