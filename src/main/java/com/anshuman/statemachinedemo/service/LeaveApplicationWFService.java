package com.anshuman.statemachinedemo.service;

import static com.anshuman.statemachinedemo.workflow.data.constant.LeaveAppConstants.LEAVE_APP_WF_V1;

import com.anshuman.statemachinedemo.exception.StateMachineException;
import com.anshuman.statemachinedemo.exception.WorkflowException;
import com.anshuman.statemachinedemo.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.statemachinedemo.model.persist.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.statemachinedemo.model.repository.projection.LAWFProjection;
import com.anshuman.statemachinedemo.util.ReactiveHelper;
import com.anshuman.statemachinedemo.util.StringUtil;
import com.anshuman.statemachinedemo.workflow.data.dto.EventResultDTO;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import com.anshuman.statemachinedemo.model.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.statemachinedemo.model.repository.projection.LAWFProjection;
import com.anshuman.statemachinedemo.workflow.data.dto.EventResultDTO;
import com.anshuman.statemachinedemo.workflow.data.dto.WorkflowEventLogDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaveApplicationWFService {

    private final DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, LeaveAppWorkFlowInstanceEntity> stateMachineAdapter;

    private final LeaveAppWorkflowInstanceRepository leaveAppRepository;

    private final WorkflowEventLogService workflowEventLogService;

    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityDoesNotExist(entity);
        stateMachineAdapter.persist(stateMachineAdapter.create(LEAVE_APP_WF_V1), entity);
        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);
        return savedEntity;
    }

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

    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull Long id, LeaveAppEvent event) {
        LeaveAppWorkFlowInstanceEntity entity = getLeaveApplicationById(id);
        return updateLeaveApplication(entity, event);
    }

    @Transactional
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent event) {
        if (event != null) {
            passEventsToEntityStateMachine(entity, event);
        }
        LeaveAppWorkFlowInstanceEntity updatedEntity = leaveAppRepository.save(entity);
        log.debug("Updated Entity: {}", updatedEntity);
        return updatedEntity;
    }

    public boolean existsById(Long id) {
        return Optional.ofNullable(leaveAppRepository.existsByIdAndWFType(id)).orElse(false);
    }

    @Transactional
    public void deleteLeaveApplication(@NotNull Long id) {
        leaveAppRepository.deleteById(id);
        log.debug("deleted entity with id: {}", id);
    }

    private void passEventsToEntityStateMachine(LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent event) {

        // get the state machine for the given entity.
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = getStateMachineFromEntity(entity);

        // send the event to the state machine and get the result.
        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> eventResults = ReactiveHelper.stateMachineHandler(stateMachine, event);

        // throw error if the event is not accepted by the state machine.
        if (!ReactiveHelper.parseResultToBool(eventResults)) {
            String eventStr = eventResults
                .stream()
                .filter(EventResultDTO.accepted)
                .map(StringUtil::event)
                .collect(Collectors.joining(", "));

            throw new StateMachineException("Did not persist the state machine context to the database, "
                + "as the following passed event: [" + eventStr + "]" +
                " were not accepted by the statemachine of the LeaveApp with id: " + entity.getId());
        }

        // save the state machine context once event is accepted.
        saveStateMachineToEntity(stateMachine, entity);

        // log the event asynchronously once it is successfully processed by the statemachine.
        var wfEventLogDto = WorkflowEventLogDTO.builder().companyId(entity.getCompanyId())
            .branchId(entity.getBranchId()).typeId(entity.getTypeId()).instanceId(entity.getId())
            .state(stateMachine.getState().getId().toString()).event(event.toString())
            .actionDate(LocalDateTime.now()).completed((short) (stateMachine.isComplete() ? 1 : 0))
            .actionBy(0L).userRole((short) 0).build();
        workflowEventLogService.logEvent(wfEventLogDto);

    }

    private void saveStateMachineToEntity(@NotNull StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        @NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityHasStateMachineContext(entity);
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());
    }

    private StateMachine<LeaveAppState, LeaveAppEvent> getStateMachineFromEntity(LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityHasStateMachineContext(entity);
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = stateMachineAdapter.restore(LEAVE_APP_WF_V1, entity);
        log.debug("entity id: {}, current state: {}, stateMachine current state: {}", entity.getId(),
            entity.getCurrentState(), stateMachine.getState().getId());
        return stateMachine;
    }

    private void validateThatEntityDoesNotExist(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getId() != null && existsById(entity.getId())) {
            throw new WorkflowException("Cannot save LeaveAppWorkflowInstance. An entry with this id already exists");
        }
    }

    private static void validateThatEntityHasStateMachineContext(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getStateMachineContext() == null) {
            throw new StateMachineException("No state machine context found for the entity");
        }
    }

}
