package com.anshuman.workflow.service;


import static com.anshuman.workflow.statemachine.data.constant.StateMachineConstants.KEY_RETURN_COUNT;
import static com.anshuman.workflow.statemachine.data.constant.StateMachineConstants.KEY_ROLL_BACK_COUNT;
import static com.anshuman.workflow.statemachine.event.LoanAppEvent.E_INITIALIZE;

import com.anshuman.workflow.data.model.entity.LoanAppWorkflowInstanceEntity;
import com.anshuman.workflow.data.model.repository.LoanAppWorkflowInstanceRepository;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.event.LoanAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.persist.StateMachineService;
import com.anshuman.workflow.statemachine.state.LoanAppState;
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
public class LoanAppWFService {

    private final StateMachineService<LoanAppState, LoanAppEvent, LoanAppWorkflowInstanceEntity> stateMachineService;

    private final LoanAppWorkflowInstanceRepository loanAppRepository;


    /* CREATE */
    @Transactional
    public LoanAppWorkflowInstanceEntity createLeaveApplication(@NotNull LoanAppWorkflowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);

        var stateMachine = stateMachineService.createStateMachine(entity, entity.getTypeId());

        var eventDto = PassEventDto.builder().event(E_INITIALIZE.name()).actionBy(entity.getCreatedByUserId()).build();
        var result = LoanAppEvent.passEvent( stateMachine, eventDto);

        List<EventResultDTO<LoanAppState, LoanAppEvent>> results = result.getSecond();
        if (results.isEmpty()) {
            throw new WorkflowException("Could not save LeaveApplication entity", new StateMachineException("StateMachine did not accept initialize event"));
        }

        stateMachine = result.getFirst();
        stateMachineService.saveStateMachineToEntity(stateMachine, entity, eventDto, false);

        LoanAppWorkflowInstanceEntity savedEntity = loanAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);

        stateMachineService.writeToLog(entity, stateMachine, eventDto);

        return savedEntity;
    }


    /* READ */
    @Transactional(readOnly = true)
    public LoanAppWorkflowInstanceEntity getLeaveApplicationById(@NotNull Long id) {
        return loanAppRepository.findById(id)
            .orElseGet(() -> {
                log.warn("No entity found with id: {}", id);
                return null;
            });
    }

    @Transactional(readOnly = true)
    public List<LoanAppWorkflowInstanceEntity> getAll() {
        return loanAppRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return Optional.ofNullable(loanAppRepository.existsByIdAndWFType(id)).orElse(false);
    }


    /* UPDATE */
    @Transactional
    public List<EventResponseDto> passEvent(PassEventDto eventDto) {
        LoanAppWorkflowInstanceEntity entity = getLeaveApplicationById(eventDto.getWorkflowInstance());
        if (entity == null) {
            log.warn("returning empty results as entity was null");
            return Collections.emptyList();
        }

        var stateMachine = stateMachineService.getStateMachineFromEntity(entity, entity.getTypeId());

        var result = LoanAppEvent.passEvent( stateMachine, eventDto);
        var resultDTOList = result.getSecond();
        if (resultDTOList.isEmpty()) {
            log.warn("the statemachine returned empty results.");
            return Collections.emptyList();
        }

        stateMachine = result.getFirst();
        stateMachineService.saveStateMachineToEntity(stateMachine, entity, eventDto, true);

        var updatedEntity = updateLeaveApplication(eventDto, stateMachine, entity);
        log.debug("updated entity: {}",updatedEntity);

        return EventResponseDto.fromEventResults(entity.getId(), entity.getTypeId(), resultDTOList);
    }

    @Transactional
    public LoanAppWorkflowInstanceEntity updateLeaveApplication(PassEventDto eventDto, StateMachine<LoanAppState, LoanAppEvent> stateMachine,
        LoanAppWorkflowInstanceEntity entity) {

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
        var updatedEntity = loanAppRepository.save(entity);
        log.debug("Updated Entity: {}", updatedEntity);
        return updatedEntity;
    }


    /* DELETE */
    @Transactional
    public void deleteLeaveApplication(@NotNull Long id) {
        loanAppRepository.deleteById(id);
        log.debug("deleted entity with id: {}", id);
    }


    /* OTHER METHODS */
    private void validateThatEntityDoesNotExist(@NotNull LoanAppWorkflowInstanceEntity entity) {
        if (entity.getId() != null && existsById(entity.getId())) {
            throw new WorkflowException("Cannot save LeaveAppWorkflowInstance. An entry with this id already exists");
        }
    }
}
