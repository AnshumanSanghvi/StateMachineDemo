package com.anshuman.workflow.data.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class WorkflowTypeDto {

    // base entity
    Long companyId;
    Integer branchId;
    LocalDateTime createDate;
    LocalDateTime updateDate;
    LocalDateTime deleteDate;

    // workflow type
    LocalDateTime withEffectFromDate;
    int workflowTypeId;
    Short isActive;
    Long updateByUserId;

    // workflow properties
    Boolean hasParallelApproval;
    Boolean hasRepeatableApprovers;
    Boolean canRollBackApproval;
    Boolean canAdminApproveWorkflow;
    List<Long> adminRoleIds;
    Integer maximumChangeRequestThreshold;
    Integer maximumRollbackApprovalThreshold;
    Map<Integer, Long> reviewerMap;

    public static WorkflowTypeEntity toEntity(WorkflowTypeDto dto) {
        WorkflowTypeEntity entity = new WorkflowTypeEntity();

        entity.setCompanyId(dto.companyId);
        entity.setBranchId(dto.branchId);
        if (dto.createDate != null) entity.setCreatedDate(dto.createDate);
        if (dto.updateDate != null) entity.setUpdatedDate(dto.updateDate);
        if (dto.deleteDate != null) entity.setDeletedDate(dto.deleteDate);

        // to create a new workflow type, its entry should be present in the WorkflowType enum.
        WorkflowType type = WorkflowType.fromId(dto.workflowTypeId);
        entity.setTypeId(type);
        entity.setName(type);
        entity.setWithEffectFromDate(dto.withEffectFromDate);
        if (dto.isActive != null) entity.setIsActive(dto.isActive);
        if (dto.updateByUserId != null) entity.setUpdateByUserId(dto.updateByUserId);

        WorkflowProperties properties =  new WorkflowProperties();
        if (dto.hasParallelApproval != null) properties.setHasParallelApproval(dto.hasParallelApproval);
        if (dto.hasRepeatableApprovers != null) properties.setHasRepeatableApprovers(dto.hasRepeatableApprovers);
        if (dto.canRollBackApproval != null) properties.setCanRollBackApproval(dto.canRollBackApproval);
        if (dto.canAdminApproveWorkflow != null) properties.setCanAdminApproveWorkflow(dto.canAdminApproveWorkflow);
        if (dto.adminRoleIds != null) properties.setAdminRoleIds(dto.adminRoleIds);
        if (dto.maximumChangeRequestThreshold != null) properties.setMaximumChangeRequestThreshold(dto.maximumChangeRequestThreshold);
        if (dto.maximumRollbackApprovalThreshold != null) properties.setMaximumRollbackApprovalThreshold(dto.maximumRollbackApprovalThreshold);
        properties.setReviewerMap(dto.reviewerMap);

        entity.setWorkflowProperties(properties);

        return entity;
    }

}
