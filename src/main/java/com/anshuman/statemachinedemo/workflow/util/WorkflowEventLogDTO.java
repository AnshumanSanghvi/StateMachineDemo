package com.anshuman.statemachinedemo.workflow.data.dto;

import com.anshuman.statemachinedemo.model.entity.WorkflowEventLogEntity;
import com.anshuman.statemachinedemo.workflow.data.enums.WorkflowType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class WorkflowEventLogDTO {

    private Long id;

    private Long companyId;

    private Long branchId;

    private WorkflowType typeId;

    private Long instanceId;

    private String state;

    private String event;

    private LocalDateTime actionDate;

    private Long actionBy;

    private Short userRole;

    private Short completed;

    public static WorkflowEventLogEntity toEntity(WorkflowEventLogDTO dto) {
        return WorkflowEventLogEntity.builder()
            .id(dto.getId())
            .companyId(dto.getCompanyId())
            .branchId(dto.getBranchId())
            .typeId(dto.getTypeId())
            .instanceId(dto.getInstanceId())
            .state(dto.getState())
            .event(dto.getEvent())
            .actionDate(dto.getActionDate())
            .actionBy(dto.getActionBy())
            .userRole(dto.getUserRole())
            .completed(dto.getCompleted())
            .build();
    }

}