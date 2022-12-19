package com.anshuman.statemachinedemo.workflow.service;

import com.anshuman.statemachinedemo.workflow.model.entity.WorkflowTypeEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.WorkflowType;
import com.anshuman.statemachinedemo.workflow.repository.WorkflowTypeRepository;
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
            throw new RuntimeException("Cannot create new workflow type, there already exists a type for the given type id");
    }
}
