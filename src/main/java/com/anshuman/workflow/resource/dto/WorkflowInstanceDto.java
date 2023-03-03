package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.statemachine.data.Pair;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkflowInstanceDto {

    public WorkflowInstanceDto(BaseDto baseDto, WorkflowType typeId, Long createdByUserId, Long updatedByUserId, Long deletedByUserId, Short workflowVersion,
        List<Pair<Integer, Long>> reviewers) {
        this.baseDto = baseDto;
        this.typeId = typeId;
        this.createdByUserId = createdByUserId;
        this.updatedByUserId = updatedByUserId;
        this.deletedByUserId = deletedByUserId;
        this.workflowVersion = workflowVersion;
        this.reviewers = reviewers;
    }

    // base entity
    BaseDto baseDto;

    // workflow instance entity
    WorkflowType typeId;
    Long createdByUserId;
    Long updatedByUserId;
    Long deletedByUserId;
    Short timesRolledBackCount = 0;
    Short timesReturnedCount = 0;
    Short workflowVersion;
    List<Pair<Integer, Long>> reviewers;
}
