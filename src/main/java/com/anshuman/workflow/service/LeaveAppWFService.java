package com.anshuman.workflow.service;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_RETURN_COUNT;
import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.KEY_ROLL_BACK_COUNT;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.statemachine.LeaveAppStateMachineService;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveAppWFService {

    private final LeaveAppStateMachineService leaveAppStateMachineService;

    private final LeaveAppWorkflowInstanceRepository leaveAppRepository;


    /* CREATE */
    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);

        var stateMachine = leaveAppStateMachineService.createStateMachine(entity);

        var eventDto = PassEventDto.builder().event(LeaveAppEvent.E_INITIALIZE.name()).actionBy(entity.getCreatedByUserId()).build();
        var result = leaveAppStateMachineService.passEvent(null, stateMachine, eventDto);

        if (result.getSecond().isEmpty()) {
            throw new WorkflowException("Could not save LeaveApplication", new StateMachineException("StateMachine did not accept initialize event"));
        }

        leaveAppStateMachineService.saveStateMachineToEntity(result.getFirst(), entity, eventDto, false);

        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);

        return savedEntity;
    }


    /* READ */
    @Transactional(readOnly = true)
    public LeaveAppWorkFlowInstanceEntity getLeaveApplicationById(@NotNull Long id) {
        return leaveAppRepository.findById(id)
            .orElseGet(() -> {
                log.warn("No entity found with id: {}", id);
                return null;
            });
    }

    @Transactional(readOnly = true)
    public List<LeaveAppWorkFlowInstanceEntity> getAll() {
        return leaveAppRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return Optional.ofNullable(leaveAppRepository.existsByIdAndWFType(id)).orElse(false);
    }


    /* UPDATE */
    @Transactional
    public List<EventResponseDto> passEvent(PassEventDto eventDto) {
        LeaveAppWorkFlowInstanceEntity entity = getLeaveApplicationById(eventDto.getWorkflowInstance());
        if (entity == null) {
            log.warn("returning empty results as entity was null");
            return Collections.emptyList();
        }

        var stateMachine = leaveAppStateMachineService.getStateMachineFromEntity(entity);

        var result = leaveAppStateMachineService.passEvent(entity.getId(), stateMachine, eventDto);
        var resultDTOList = result.getSecond();
        if (resultDTOList.isEmpty()) {
            log.warn("the statemachine returned empty results.");
            return Collections.emptyList();
        }

        stateMachine = result.getFirst();
        leaveAppStateMachineService.saveStateMachineToEntity(stateMachine, entity, eventDto, true);

        updateLeaveApplication(eventDto, stateMachine, entity);

        return EventResponseDto.fromEventResults(entity.getId(), entity.getTypeId(), resultDTOList);
    }

    @Transactional
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(PassEventDto eventDto, StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        LeaveAppWorkFlowInstanceEntity entity) {

        // update entity details
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedByUserId(eventDto.getActionBy());
        entity.setCurrentState(stateMachine.getState().getId());
        var map = stateMachine.getExtendedState().getVariables();
        int returnCount = (Integer) map.getOrDefault(KEY_RETURN_COUNT, 0);
        if (entity.getTimesReturnedCount() != returnCount) {
            entity.setTimesReturnedCount((short) returnCount);
        }
        int rollbackCount = (Integer) map.getOrDefault(KEY_ROLL_BACK_COUNT, 0);
        if (entity.getTimesRolledBackCount() != rollbackCount) {
            entity.setTimesRolledBackCount((short) rollbackCount);
        }

        // update entity
        var updatedEntity = leaveAppRepository.save(entity);
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
