package com.sttl.hrms.workflow.resource.dto;

import com.sttl.hrms.workflow.data.enums.LoanType;
import com.sttl.hrms.workflow.data.enums.WorkFlowTypeStateMachine;
import com.sttl.hrms.workflow.data.model.entity.LoanAppWorkflowInstanceEntity;
import com.sttl.hrms.workflow.statemachine.data.Pair;
import java.util.Comparator;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoanAppWFInstanceDto {

    WorkflowInstanceDto wfInstDto;
    Short isActive = 1;
    @NotNull Integer loanType;

    public static LoanAppWorkflowInstanceEntity toEntity(LoanAppWFInstanceDto dto) {
        var entity =  new LoanAppWorkflowInstanceEntity();

        entity.setLoanType(LoanType.fromId(dto.getLoanType()));
        entity.setIsActive(dto.getIsActive());

        WorkFlowTypeStateMachine.LOAN_APPLICATION_STATE_MACHINES
            .getStateMachineIds()
            .stream()
            .max(Comparator.comparing(Pair::getFirst))
            .map(Pair::getSecond)
            .ifPresent(entity::setStateMachineId);

        var wfInstDto = dto.getWfInstDto();
        entity.setTypeId(wfInstDto.getTypeId());
        entity.setCreatedByUserId(wfInstDto.getCreatedByUserId());
        entity.setUpdatedByUserId(wfInstDto.getUpdatedByUserId());
        entity.setDeletedByUserId(wfInstDto.getDeletedByUserId());
        entity.setTimesRolledBackCount(wfInstDto.getTimesRolledBackCount());
        entity.setTimesReturnedCount(wfInstDto.getTimesReturnedCount());
        entity.setWorkflowVersion(wfInstDto.getWorkflowVersion());
        entity.setReviewers(wfInstDto.getReviewers());

        var baseDto = wfInstDto.getBaseDto();
        entity.setCompanyId(baseDto.getCompanyId());
        entity.setBranchId(baseDto.getBranchId());
        entity.setCreatedDate(baseDto.getCreateDate());
        entity.setUpdatedDate(baseDto.getUpdateDate());
        entity.setDeletedDate(baseDto.getDeleteDate());

        return entity;

    }
}
