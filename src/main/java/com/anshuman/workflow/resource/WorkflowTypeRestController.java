package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.dto.WorkflowTypeDto;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import com.anshuman.workflow.service.WorkflowTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/wf/type")
@RequiredArgsConstructor
public class WorkflowTypeRestController {

    private final WorkflowTypeService service;

    @PostMapping("/create")
    public String createWorkflowType(WorkflowTypeDto dto) {
        WorkflowTypeEntity entity = service.createWorkflowType(WorkflowTypeDto.toEntity(dto));
        return entity.getTypeId().toString();
    }
}
