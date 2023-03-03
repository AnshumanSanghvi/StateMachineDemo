package com.anshuman.workflow.service;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.statemachine.LeaveAppStateMachineService;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
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
public class LeaveAppWFService {

    private final LeaveAppStateMachineService leaveAppStateMachineService;

    private final LeaveAppWorkflowInstanceRepository leaveAppRepository;


    /* CREATE */
    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);
        var stateMachine = leaveAppStateMachineService.saveStateMachineForCreatedLeaveApplication(entity);
        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);
        leaveAppStateMachineService.writeToLog(entity, stateMachine, null);
        return savedEntity;
    }


    /* READ */
    public LeaveAppWorkFlowInstanceEntity getLeaveApplicationById(@NotNull Long id) {
        return leaveAppRepository.findById(id)
            .orElseGet(() -> {
                log.warn("No entity found with id: {}", id);
                return null;
            });
    }

    public List<LeaveAppWorkFlowInstanceEntity> getAll() {
        return leaveAppRepository.findAll();
    }

    public boolean existsById(Long id) {
        return Optional.ofNullable(leaveAppRepository.existsByIdAndWFType(id)).orElse(false);
    }


    /* UPDATE */
    public List<EventResponseDto> passEvent(PassEventDto eventDto) {
        LeaveAppWorkFlowInstanceEntity entity = getLeaveApplicationById(eventDto.getWorkflowInstance());
        if (entity == null)
            return null;
        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> resultDTOList = leaveAppStateMachineService.passEventsToEntityStateMachine(entity, eventDto);
        updateLeaveApplication(entity);
        return EventResponseDto.fromEventResults(entity.getId(), entity.getTypeId(), resultDTOList);
    }

    @Transactional
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
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
