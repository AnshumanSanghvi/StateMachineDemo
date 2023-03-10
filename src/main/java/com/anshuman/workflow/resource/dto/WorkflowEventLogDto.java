package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class WorkflowEventLogDto {

    private Long id;

    @NotNull private Long companyId;

    @NotNull private Integer branchId;

    private Integer typeId;

    private Long instanceId;

    private String state;

    private String event;

    private LocalDateTime actionDate;

    private Long actionBy;

    private Short userRole;

    private Short completed;

    private String comment;

    public static WorkflowEventLogEntity toEntity(WorkflowEventLogDto dto) {
        return WorkflowEventLogEntity.builder()
            .id(dto.getId())
            .companyId(dto.getCompanyId())
            .branchId(dto.getBranchId())
            .typeId(WorkflowType.fromId(dto.getTypeId()))
            .instanceId(dto.getInstanceId())
            .state(dto.getState())
            .event(dto.getEvent())
            .actionDate(dto.getActionDate())
            .actionBy(dto.getActionBy())
            .userRole(dto.getUserRole())
            .completed(dto.getCompleted())
            .comment(dto.getComment())
            .build();
    }

}
