package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class WorkflowEventLogDto {

    private Long id;

    private Long companyId;

    private Integer branchId;

    private WorkflowType typeId;

    private Long instanceId;

    private String state;

    private String event;

    private LocalDateTime actionDate;

    private Long actionBy;

    private Short userRole;

    private Short completed;

    public static WorkflowEventLogEntity toEntity(WorkflowEventLogDto dto) {
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
