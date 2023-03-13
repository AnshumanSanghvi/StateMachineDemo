package com.sttl.hrms.workflow.resource.dto;

import com.sttl.hrms.workflow.data.enums.LeaveType;
import com.sttl.hrms.workflow.data.enums.WorkFlowTypeStateMachine;
import com.sttl.hrms.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.sttl.hrms.workflow.statemachine.data.Pair;
import java.util.Comparator;
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

        // set the latest version stateMachineId
        WorkFlowTypeStateMachine.LEAVE_APPLICATION_STATE_MACHINES
            .getStateMachineIds()
            .stream()
            .max(Comparator.comparing(Pair::getFirst))
            .map(Pair::getSecond)
            .ifPresent(leaveAppWFEntity::setStateMachineId);

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
