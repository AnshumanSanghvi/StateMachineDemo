package com.sttl.hrms.workflow.resource.dto;

import com.sttl.hrms.workflow.data.model.entity.WorkflowProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkflowPropertiesDto {

    // workflow properties
    Boolean hasParallelApproval;
    Boolean hasRepeatableApprovers;
    Boolean canRollBackApproval;
    Boolean canAdminApproveWorkflow;
    List<Long> adminRoleIds;
    Integer maximumChangeRequestThreshold;
    Integer maximumRollbackApprovalThreshold;

    public static WorkflowProperties toProp(WorkflowPropertiesDto wfPropDto) {
        WorkflowProperties properties =  new WorkflowProperties();
        if (wfPropDto.hasParallelApproval != null) properties.setHasParallelApproval(wfPropDto.hasParallelApproval);
        if (wfPropDto.hasRepeatableApprovers != null) properties.setHasRepeatableApprovers(wfPropDto.hasRepeatableApprovers);
        if (wfPropDto.canRollBackApproval != null) properties.setCanRollBackApproval(wfPropDto.canRollBackApproval);
        if (wfPropDto.canAdminApproveWorkflow != null) properties.setCanAdminApproveWorkflow(wfPropDto.canAdminApproveWorkflow);
        if (wfPropDto.adminRoleIds != null) properties.setAdminRoleIds(wfPropDto.adminRoleIds);
        if (wfPropDto.maximumChangeRequestThreshold != null) properties.setMaximumChangeRequestThreshold(wfPropDto.maximumChangeRequestThreshold);
        if (wfPropDto.maximumRollbackApprovalThreshold != null) properties.setMaximumRollbackApprovalThreshold(wfPropDto.maximumRollbackApprovalThreshold);

        return properties;
    }
}


