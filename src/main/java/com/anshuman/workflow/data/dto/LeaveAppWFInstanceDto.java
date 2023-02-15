package com.anshuman.workflow.data.dto;

import com.anshuman.workflow.data.enums.LeaveType;
import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import lombok.Value;

@Value
public class LeaveAppWFInstanceDto {

    WorkflowInstanceDto wfInstDto;
    short isActive = 1;
    int leaveType;

    public static LeaveAppWorkFlowInstanceEntity toEntity(LeaveAppWFInstanceDto dto) {

        var leaveAppWFEntity = new LeaveAppWorkFlowInstanceEntity();

        // leave app workflow instance
        leaveAppWFEntity.setLeaveType(LeaveType.fromNumber(dto.getLeaveType()));
        leaveAppWFEntity.setIsActive(dto.getIsActive());

        // workflow instance
        var wfInstDto = dto.getWfInstDto();
        leaveAppWFEntity.setTypeId(wfInstDto.getTypeId());
        leaveAppWFEntity.setCreatedByUserId(wfInstDto.getCreatedByUserId());
        leaveAppWFEntity.setUpdatedByUserId(wfInstDto.getUpdatedByUserId());
        leaveAppWFEntity.setDeletedByUserId(wfInstDto.getDeletedByUserId());
        leaveAppWFEntity.setTimesRolledBackCount(wfInstDto.getTimesRolledBackCount());
        leaveAppWFEntity.setTimesReturnedCount(wfInstDto.getTimesReturnedCount());
        leaveAppWFEntity.setWorkflowVersion(wfInstDto.getWorkflowVersion());
        leaveAppWFEntity.setReviewers(wfInstDto.getReviewers());

        // base entity
        var baseDto = wfInstDto.getBaseDto();
        leaveAppWFEntity.setCompanyId(baseDto.getCompanyId());
        leaveAppWFEntity.setBranchId(baseDto.getBranchId());
        leaveAppWFEntity.setCreatedDate(baseDto.getCreateDate());
        leaveAppWFEntity.setUpdatedDate(baseDto.getUpdateDate());
        leaveAppWFEntity.setDeletedDate(baseDto.getDeleteDate());

        return leaveAppWFEntity;
    }
}
