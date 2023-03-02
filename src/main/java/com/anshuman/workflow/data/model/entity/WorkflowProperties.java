package com.anshuman.workflow.data.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowProperties implements Serializable {
    @Builder.Default private boolean hasParallelApproval = false;
    @Builder.Default private boolean hasRepeatableApprovers = false;
    @Builder.Default private boolean canRollBackApproval = true;
    @Builder.Default private boolean canAdminApproveWorkflow = true;
    @Builder.Default private List<Long> adminRoleIds = new ArrayList<>(5);
    @Builder.Default private Map<Integer, Long> reviewerMap = new HashMap<>(3);
    @Builder.Default private int maximumChangeRequestThreshold = 5;
    @Builder.Default private int maximumRollbackApprovalThreshold = 5;

    public WorkflowProperties addAdminId(Long id) {
        if (adminRoleIds == null)
            adminRoleIds = new ArrayList<>(5);
        adminRoleIds.add(id);
        return this;
    }
}