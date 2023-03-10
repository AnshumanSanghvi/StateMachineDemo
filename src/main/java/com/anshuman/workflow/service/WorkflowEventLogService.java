package com.anshuman.workflow.service;

import com.anshuman.workflow.data.model.dao.WorkflowEventLogDao;
import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import com.anshuman.workflow.data.model.repository.WorkflowEventLogRepository;
import com.anshuman.workflow.resource.dto.WorkflowEventLogDto;
import java.util.List;
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

    private final WorkflowEventLogDao workflowEventLogDAO;

    @Transactional
    public void logEvent(WorkflowEventLogDto workflowEventLogDTO) {
        log.debug("Attempting to log workflow event");
        CompletableFuture.supplyAsync(() -> save(workflowEventLogDTO));
    }

    public List<WorkflowEventLogEntity> getWorkflowEventLogsPartitionedByType(WorkflowEventLogDto workflowEventLogDto) {
        var output = workflowEventLogDAO.getWorkflowEventLogs(workflowEventLogDto);
        log.debug("event logs by typeId:\n{}", output.stream().map(WorkflowEventLogEntity::toString).collect(Collectors.joining(",\n")));
        return output;
    }

    @Transactional
    public WorkflowEventLogEntity save(WorkflowEventLogDto workflowEventLogDto) {
        var savedEntity = workflowEventLogRepository.save(WorkflowEventLogDto.toEntity(workflowEventLogDto));
        log.debug("saved workflowEventLogEntity: {}", savedEntity);
        return savedEntity;
    }

}
