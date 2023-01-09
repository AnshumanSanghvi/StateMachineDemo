package com.anshuman.statemachinedemo.service;

import com.anshuman.statemachinedemo.model.dao.WorkflowEventLogDAO;
import com.anshuman.statemachinedemo.model.entity.WorkflowEventLogEntity;
import com.anshuman.statemachinedemo.model.repository.WorkflowEventLogRepository;
import com.anshuman.statemachinedemo.workflow.data.dto.WorkflowEventLogDTO;
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
