package com.anshuman.statemachinedemo.workflow.service;

import com.anshuman.statemachinedemo.workflow.model.entity.WorkflowEventLogEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.WorkflowType;
import com.anshuman.statemachinedemo.workflow.repository.WorkflowEventLogRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WorkflowEventLogService<S, E> {

    private final WorkflowEventLogRepository<S, E> workflowEventLogRepository;

    public void logEvent(Long id, Long companyId, Long branchId, WorkflowType typeId, Long instanceId, S state, E event, Long userId,
        short userRole, short isComplete) {
        CompletableFuture.supplyAsync(() -> {
            WorkflowEventLogEntity<S, E> entity = new WorkflowEventLogEntity<>();
            entity.setCompanyId(companyId);
            entity.setBranchId(branchId);
            entity.setTypeId(typeId);
            entity.setInstanceId(instanceId);
            entity.setState(state);
            entity.setEvent(event);
            entity.setActionDate(LocalDateTime.now());
            entity.setActionBy(userId);
            entity.setUserRole(userRole);
            entity.setCompleted(isComplete);
            var savedWorkflowEventLog = workflowEventLogRepository.save(entity);
            log.debug("saved workflowEventLogEntity: {}", savedWorkflowEventLog);
            return savedWorkflowEventLog;
        });
    }

}
