package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.model.entity.WorkflowInstanceEntity;
import com.anshuman.workflow.service.WorkflowInstanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wf")
@RequiredArgsConstructor
@Slf4j
public class WorkflowInstanceRestController {

    private final WorkflowInstanceService workflowInstanceService;

    @GetMapping("/id/{id}")
    public @ResponseBody WorkflowInstanceEntity getWFInstanceById(@PathVariable("id") Long id) {
        return workflowInstanceService.findById(id);
    }

    @GetMapping("/cid/{cid}/bid/{bid}")
    public @ResponseBody List<WorkflowInstanceEntity> getWFInstancesByCompanyAndBranch(@PathVariable("cid") Long companyId,
        @PathVariable("bid") Integer branchId) {
        return workflowInstanceService.findByCompanyIdAndBranchId(companyId, branchId);
    }

    @GetMapping("/cid/{cid}/bid/{bid}/type/{typeId}")
    public @ResponseBody List<WorkflowInstanceEntity> getWFInstancesByTypeId(@PathVariable("cid") Long companyId,
        @PathVariable("bid") Integer branchId, @PathVariable("typeId") Integer typeId) {
        return workflowInstanceService.findByTypeId(companyId, branchId, typeId);
    }

}