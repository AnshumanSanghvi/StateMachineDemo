package com.anshuman.workflow.service;

import com.anshuman.workflow.data.dto.WorkflowEventLogDTO;
import com.anshuman.workflow.data.model.dao.WorkflowEventLogDAO;
import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import com.anshuman.workflow.data.model.repository.WorkflowEventLogRepository;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowEventLogService {

    private final WorkflowEventLogRepository workflowEventLogRepository;

    private final WorkflowEventLogDAO workflowEventLogDAO;

    @Transactional
    public void logEvent(WorkflowEventLogDTO workflowEventLogDTO) {
        CompletableFuture.supplyAsync(() -> {
            var savedWorkflowEventLog = workflowEventLogRepository.save(WorkflowEventLogDTO.toEntity(workflowEventLogDTO));
            log.debug("saved workflowEventLogEntity: {}", savedWorkflowEventLog);
            return savedWorkflowEventLog;
        });
    }

    public void findEventLogsByWorkflowType(WorkflowEventLogDTO workflowEventLogDTO) {
        var output = workflowEventLogDAO.getWorkflowEventLogByType(workflowEventLogDTO);
        log.debug("event logs by typeId:\n{}",
            output.stream().map(WorkflowEventLogEntity::toString)
                .collect(Collectors.joining(",\n")));
    }

}
