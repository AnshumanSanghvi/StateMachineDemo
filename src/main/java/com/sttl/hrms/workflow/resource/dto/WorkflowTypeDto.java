package com.sttl.hrms.workflow.resource.dto;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity.WorkflowProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkflowTypeDto {

    // base entity
    @NotNull Long companyId;
    @NotNull Integer branchId;
    LocalDateTime createDate;
    LocalDateTime updateDate;
    LocalDateTime deleteDate;
    Long createdByUserId;
    Long updatedByUserId;
    Long deletedByUserId;

    // workflow type
    LocalDateTime withEffectFromDate;
    Integer workflowTypeId;
    Short isActive;

    // workflow properties
    WorkflowPropertiesDto wfPropDto;

    public static WorkflowTypeEntity toEntity(WorkflowTypeDto dto) {
        WorkflowTypeEntity entity = new WorkflowTypeEntity();

        entity.setCompanyId(dto.getCompanyId());
        entity.setBranchId(dto.getBranchId());
        if (dto.getCreateDate() != null) entity.setCreatedDate(dto.getCreateDate());
        if (dto.getUpdateDate() != null) entity.setUpdatedDate(dto.getUpdateDate());
        if (dto.getDeleteDate() != null) entity.setDeletedDate(dto.getDeleteDate());

        // to create a new workflow type, its entry should be present in the WorkflowType enum.
        WorkflowType type = WorkflowType.fromId(dto.workflowTypeId);
        entity.setTypeId(type);
        entity.setName(type);
        entity.setWithEffectFromDate(dto.withEffectFromDate);
        if (dto.isActive != null) entity.setIsActive(dto.isActive);
        if (dto.updatedByUserId != null) entity.setUpdatedByUserId(dto.updatedByUserId);
        if (dto.createdByUserId != null) entity.setCreatedByUserId(dto.createdByUserId);
        if (dto.deletedByUserId != null) entity.setDeletedByUserId(dto.deletedByUserId);

        WorkflowProperties properties = WorkflowPropertiesDto.toProp(dto.wfPropDto);
        entity.setWorkflowProperties(properties);

        return entity;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class WorkflowPropertiesDto {

        // workflow properties
        Boolean parallelApproval;
        Boolean repeatableApprovers;
        Boolean rollBackApproval;
        Boolean adminApproveWorkflow;
        List<Long> adminRoleIds;
        Integer changeReqMaxCount;
        Integer rollbackMaxCount;

        public static WorkflowProperties toProp(WorkflowPropertiesDto wfPropDto) {
            WorkflowProperties properties = new WorkflowProperties();
            if (wfPropDto.parallelApproval != null) properties.setParallelApproval(wfPropDto.parallelApproval);
            if (wfPropDto.repeatableApprovers != null)
                properties.setRepeatableApprovers(wfPropDto.repeatableApprovers);
            if (wfPropDto.rollBackApproval != null) properties.setRollBackApproval(wfPropDto.rollBackApproval);
            if (wfPropDto.adminApproveWorkflow != null)
                properties.setAdminApproveWorkflow(wfPropDto.adminApproveWorkflow);
            if (wfPropDto.adminRoleIds != null) properties.setAdminRoleIds(wfPropDto.adminRoleIds);
            if (wfPropDto.changeReqMaxCount != null)
                properties.setChangeReqMaxCount(wfPropDto.changeReqMaxCount);
            if (wfPropDto.rollbackMaxCount != null)
                properties.setRollbackMaxCount(wfPropDto.rollbackMaxCount);

            return properties;
        }
    }
}
