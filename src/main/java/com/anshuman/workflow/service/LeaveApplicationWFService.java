package com.anshuman.workflow.service;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.workflow.data.model.repository.projection.LAWFProjection;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.statemachine.LeaveAppStateMachineService;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
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
public class LeaveApplicationWFService {

    private final LeaveAppStateMachineService leaveAppStateMachineService;

    private final LeaveAppWorkflowInstanceRepository leaveAppRepository;


    /* CREATE */
    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);
        leaveAppStateMachineService.saveStateMachineForCreatedLeaveApplication(entity);
        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);
        return savedEntity;
    }


    /* READ */
    public LeaveAppWorkFlowInstanceEntity getLeaveApplicationById(@NotNull Long id) {
        return leaveAppRepository.findPartialById(id)
            .map(lawf -> {
                LeaveAppWorkFlowInstanceEntity entity = LAWFProjection.toEntity(lawf);
                log.debug("found entity: {}", entity);
                return entity;
            })
            .orElseGet(() -> {
                log.warn("No entity found with id: {}", id);
                return null;
            });
    }

    public boolean existsById(Long id) {
        return Optional.ofNullable(leaveAppRepository.existsByIdAndWFType(id)).orElse(false);
    }


    /* UPDATE */
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull Long id, LeaveAppEvent event) {
        LeaveAppWorkFlowInstanceEntity entity = leaveAppRepository.getReferenceById(id);
        return updateLeaveApplication(entity, event);
    }

    @Transactional
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent event) {
        if (event != null) {
            leaveAppStateMachineService.passEventsToEntityStateMachine(entity, event);
        }
        LeaveAppWorkFlowInstanceEntity updatedEntity = leaveAppRepository.save(entity);
        log.debug("Updated Entity: {}", updatedEntity);
        return updatedEntity;
    }


    /* DELETE */
    @Transactional
    public void deleteLeaveApplication(@NotNull Long id) {
        leaveAppRepository.deleteById(id);
        log.debug("deleted entity with id: {}", id);
    }


    /* OTHER METHODS */
    private void validateThatEntityDoesNotExist(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getId() != null && existsById(entity.getId())) {
            throw new WorkflowException("Cannot save LeaveAppWorkflowInstance. An entry with this id already exists");
        }
    }

}
