package com.anshuman.workflow.service;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.statemachine.LeaveAppStateMachineService;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import java.util.Collections;
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

        var stateMachine = leaveAppStateMachineService.createStateMachine(entity);

        var eventDto = PassEventDto.builder().event(LeaveAppEvent.E_INITIALIZE.name()).actionBy(entity.getCreatedByUserId()).build();
        var result = leaveAppStateMachineService.passEventsToStateMachine(null, stateMachine, eventDto);

        if (result.getSecond().isEmpty()) {
            throw new WorkflowException("Could not save LeaveApplication", new StateMachineException("StateMachine did not accept initialize event"));
        }

        leaveAppStateMachineService.saveStateMachineToEntity(result.getFirst(), entity, LeaveAppEvent.E_INITIALIZE, false);

        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);

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
        if (entity == null) {
            log.warn("returning empty results as entity was null");
            return Collections.emptyList();
        }

        var stateMachine = leaveAppStateMachineService.getStateMachineFromEntity(entity);

        var result = leaveAppStateMachineService.passEventsToStateMachine(entity.getId(), stateMachine,
            eventDto);
        var resultDTOList = result.getSecond();
        if (resultDTOList.isEmpty()) {
            log.warn("the statemachine returned empty results.");
            return Collections.emptyList();
        }

        stateMachine = result.getFirst();
        leaveAppStateMachineService.saveStateMachineToEntity(stateMachine, entity, LeaveAppEvent.getByName(eventDto.getEvent()), true);

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
