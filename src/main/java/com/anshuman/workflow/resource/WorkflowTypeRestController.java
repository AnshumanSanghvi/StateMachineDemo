package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import com.anshuman.workflow.resource.dto.WorkflowTypeDto;
import com.anshuman.workflow.service.WorkflowTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wf/type")
@RequiredArgsConstructor
@Slf4j
public class WorkflowTypeRestController {

    private final WorkflowTypeService workflowTypeService;

    @PostMapping("/")
    public String createWorkflowType(@RequestBody WorkflowTypeDto dto) {
        log.debug("workflow type input: {}", dto);
        WorkflowTypeEntity entity = workflowTypeService.createWorkflowType(WorkflowTypeDto.toEntity(dto));
        return entity.getTypeId().toString();
    }

    @GetMapping("/")
    public @ResponseBody List<WorkflowTypeEntity> getWorkflowTypes() {
        return workflowTypeService.getAll();
    }

    @GetMapping("/{typeId}")
    public @ResponseBody WorkflowTypeEntity getByTypeId(@PathVariable("typeId") Integer typeId) {
        return workflowTypeService.findByTypeId(WorkflowType.fromId(typeId));
    }

    @DeleteMapping("/{typeId}")
    public void deleteByTypeId(@PathVariable("typeId") Integer typeId) {
        workflowTypeService.deleteByTypeId(WorkflowType.fromId(typeId));
    }
}
