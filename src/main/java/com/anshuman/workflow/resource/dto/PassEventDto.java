package com.anshuman.workflow.resource.dto;

import com.anshuman.workflow.data.enums.WorkflowType;
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
    Long workflowInstance;
    String event;
    Long actionBy;
    @Nullable Integer orderNo;
    @Nullable String comment;
}
