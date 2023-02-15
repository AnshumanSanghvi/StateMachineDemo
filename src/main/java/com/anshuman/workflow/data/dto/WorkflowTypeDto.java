package com.anshuman.workflow.data.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class WorkflowTypeDto {

    // base entity
    BaseDto baseDto;

    // workflow type
    LocalDateTime withEffectFromDate;
    int workflowTypeId;
    Short isActive;
    Long updateByUserId;

    // workflow properties
    WorkflowPropertiesDto wfPropDto;

    public static WorkflowTypeEntity toEntity(WorkflowTypeDto dto) {
        WorkflowTypeEntity entity = new WorkflowTypeEntity();

        entity.setCompanyId(dto.baseDto.getCompanyId());
        entity.setBranchId(dto.baseDto.getBranchId());
        if (dto.baseDto.getCreateDate() != null) entity.setCreatedDate(dto.baseDto.getCreateDate());
        if (dto.baseDto.getUpdateDate() != null) entity.setUpdatedDate(dto.baseDto.getUpdateDate());
        if (dto.baseDto.getDeleteDate() != null) entity.setDeletedDate(dto.baseDto.getDeleteDate());

        // to create a new workflow type, its entry should be present in the WorkflowType enum.
        WorkflowType type = WorkflowType.fromId(dto.workflowTypeId);
        entity.setTypeId(type);
        entity.setName(type);
        entity.setWithEffectFromDate(dto.withEffectFromDate);
        if (dto.isActive != null) entity.setIsActive(dto.isActive);
        if (dto.updateByUserId != null) entity.setUpdateByUserId(dto.updateByUserId);

        WorkflowProperties properties =  WorkflowPropertiesDto.toProp(dto.wfPropDto);
        entity.setWorkflowProperties(properties);

        return entity;
    }

}
