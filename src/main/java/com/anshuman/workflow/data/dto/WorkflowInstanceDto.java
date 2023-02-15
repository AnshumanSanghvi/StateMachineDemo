package com.anshuman.workflow.data.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.statemachine.data.Pair;
import java.util.List;
import lombok.Value;

@Value
public class WorkflowInstanceDto {

    // base entity
    BaseDto baseDto;

    // workflow instance entity
    WorkflowType typeId;
    Long createdByUserId;
    Long updatedByUserId;
    Long deletedByUserId;
    short timesRolledBackCount;
    short timesReturnedCount;
    short workflowVersion;
    List<Pair<Integer, Long>> reviewers;
}
