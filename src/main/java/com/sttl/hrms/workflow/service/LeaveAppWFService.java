package com.sttl.hrms.workflow.service;

import static com.sttl.hrms.workflow.statemachine.data.constant.StateMachineConstants.KEY_RETURN_COUNT;
import static com.sttl.hrms.workflow.statemachine.data.constant.StateMachineConstants.KEY_ROLL_BACK_COUNT;
import static com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent.E_INITIALIZE;

import com.sttl.hrms.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.sttl.hrms.workflow.data.model.repository.LeaveAppWorkflowInstanceRepository;
import com.sttl.hrms.workflow.exception.WorkflowException;
import com.sttl.hrms.workflow.resource.dto.EventResponseDto;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.data.dto.EventResultDTO;
import com.sttl.hrms.workflow.statemachine.event.LeaveAppEvent;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.persist.StateMachineService;
import com.sttl.hrms.workflow.statemachine.state.LeaveAppState;
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

    private final StateMachineService<LeaveAppState, LeaveAppEvent, LeaveAppWorkFlowInstanceEntity> stateMachineService;

    private final LeaveAppWorkflowInstanceRepository leaveAppRepository;


    /* CREATE */
    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);

        var stateMachine = stateMachineService.createStateMachine(entity, entity.getTypeId());

        var eventDto = PassEventDto.builder().event(E_INITIALIZE.name()).actionBy(entity.getCreatedByUserId()).build();
        var result = LeaveAppEvent.passEvent( stateMachine, eventDto);

        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> results = result.getSecond();
        if (results.isEmpty()) {
            throw new WorkflowException("Could not save LeaveApplication entity", new StateMachineException("StateMachine did not accept initialize event"));
        }

        stateMachine = result.getFirst();
        stateMachineService.saveStateMachineToEntity(stateMachine, entity, eventDto, false);

        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);

        stateMachineService.writeToLog(entity, stateMachine, eventDto);

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

        var stateMachine = stateMachineService.getStateMachineFromEntity(entity, entity.getTypeId());

        var result = LeaveAppEvent.passEvent( stateMachine, eventDto);
        var resultDTOList = result.getSecond();
        if (resultDTOList.isEmpty()) {
            log.warn("the statemachine returned empty results.");
            return Collections.emptyList();
        }

        stateMachine = result.getFirst();
        stateMachineService.saveStateMachineToEntity(stateMachine, entity, eventDto, true);

        var updatedEntity = updateLeaveApplication(eventDto, stateMachine, entity);
        log.debug("updated entity: {}", updatedEntity);

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
