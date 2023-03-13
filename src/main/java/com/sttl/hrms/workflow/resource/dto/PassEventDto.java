package com.sttl.hrms.workflow.resource.dto;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PassEventDto {
    WorkflowType workflowType;
    @NotNull Long workflowInstance;
    @NotNull String event;
    @NotNull Long actionBy;
    @Nullable Integer orderNo;
    @Nullable String comment;
}
