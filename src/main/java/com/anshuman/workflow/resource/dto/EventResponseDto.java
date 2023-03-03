package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDto {
    private Long workflowInstance;
    private String workflowType;
    private String currentState;
    private String event;
    private String resultType;
    private boolean isComplete;

    public static <S, E> EventResponseDto fromEventResult(Long id, WorkflowType type, EventResultDTO<S, E> eventResultDTO) {
        return EventResponseDto.builder()
            .workflowInstance(id)
            .workflowType(type.getName())
            .currentState(eventResultDTO.getCurrentState().toString())
            .event(eventResultDTO.getEvent().toString())
            .resultType(eventResultDTO.getResultType().name())
            .isComplete(eventResultDTO.isComplete())
            .build();
    }

    public static <S, E>List<EventResponseDto> fromEventResults(Long id, WorkflowType type, List<EventResultDTO<S, E>> eventResultDTOList) {
        return eventResultDTOList
            .stream()
            .map(result -> EventResponseDto.fromEventResult(id, type, result))
            .toList();
    }
}
