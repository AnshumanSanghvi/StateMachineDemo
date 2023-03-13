package com.sttl.hrms.workflow.service;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity;
import com.sttl.hrms.workflow.data.model.repository.WorkflowTypeRepository;
import com.sttl.hrms.workflow.exception.WorkflowException;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkflowTypeService {

    private final WorkflowTypeRepository workflowTypeRepository;

    @Transactional
    public WorkflowTypeEntity createWorkflowType(@NotNull WorkflowTypeEntity entity) {
        validateThatWorkflowTypeDoesNotExist(entity.getTypeId());
        WorkflowTypeEntity savedEntity = workflowTypeRepository.save(entity);
        log.debug("Saved workflowTypeEntity: {}", savedEntity);
        return savedEntity;
    }

    public boolean existsByTypeId(WorkflowType workflowType) {
        return Optional.ofNullable(workflowTypeRepository.existsByTypeId(workflowType)).orElse(false);
    }

    private void validateThatWorkflowTypeDoesNotExist(WorkflowType workflowType) {
        if(existsByTypeId(workflowType))
            throw new WorkflowException("Cannot create new statemachine type, there already exists a type for the given type id");
    }

    public List<WorkflowTypeEntity> getAll() {
        return workflowTypeRepository.findAll();
    }

    public WorkflowTypeEntity findByTypeId(WorkflowType workflowType) {
        return workflowTypeRepository.findByTypeId(workflowType).orElse(null);
    }

    @Transactional
    public void deleteByTypeId(WorkflowType workflowType) {
        workflowTypeRepository.deleteByTypeId(workflowType);
    }
}
