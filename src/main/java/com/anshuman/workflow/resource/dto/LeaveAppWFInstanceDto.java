package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.LeaveType;
import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveAppWFInstanceDto {

    public LeaveAppWFInstanceDto(WorkflowInstanceDto wfInstDto, Integer leaveType) {
        this.wfInstDto = wfInstDto;
        this.leaveType = leaveType;
    }

    WorkflowInstanceDto wfInstDto;
    Short isActive = 1;
    Integer leaveType;

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
