package com.anshuman.workflow.service;

import com.anshuman.workflow.data.dto.WorkflowEventLogDto;
import com.anshuman.workflow.data.model.dao.WorkflowEventLogDao;
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

    private final WorkflowEventLogDao workflowEventLogDAO;

    @Transactional
    public void logEvent(WorkflowEventLogDto workflowEventLogDTO) {
        CompletableFuture.supplyAsync(() -> {
            var savedWorkflowEventLog = workflowEventLogRepository.save(WorkflowEventLogDto.toEntity(workflowEventLogDTO));
            log.debug("saved workflowEventLogEntity: {}", savedWorkflowEventLog);
            return savedWorkflowEventLog;
        });
    }

    public void findEventLogsByWorkflowType(WorkflowEventLogDto workflowEventLogDTO) {
        var output = workflowEventLogDAO.getWorkflowEventLogByType(workflowEventLogDTO);
        log.debug("event logs by typeId:\n{}",
            output.stream().map(WorkflowEventLogEntity::toString)
                .collect(Collectors.joining(",\n")));
    }

}
