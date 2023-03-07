package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import com.anshuman.workflow.resource.dto.WorkflowEventLogDto;
import com.anshuman.workflow.service.WorkflowEventLogService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wf/log")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WorkflowEventLogRestResource {

    private final WorkflowEventLogService workflowEventLogService;

    @GetMapping("/")
    public @ResponseBody Map<String, List<WorkflowEventLogEntity>> getByWorkflowType(@RequestBody @Valid WorkflowEventLogDto eventLogDto) {
        return workflowEventLogService.getWorkflowEventLogsPartitionedByType(eventLogDto)
            .stream()
            .collect(Collectors.groupingBy(e -> e.getTypeId().getName()));
    }
}
