package com.anshuman.statemachinedemo.workflow.service;

import static com.anshuman.statemachinedemo.workflow.constant.LeaveAppConstants.LEAVE_APP_WF_V1;

import com.anshuman.statemachinedemo.workflow.exception.StateMachineException;
import com.anshuman.statemachinedemo.workflow.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import com.anshuman.statemachinedemo.workflow.persist.DefaultStateMachineAdapter;
import com.anshuman.statemachinedemo.workflow.repository.LeaveAppWorkflowInstanceRepository;
import com.anshuman.statemachinedemo.workflow.util.EventResult;
import com.anshuman.statemachinedemo.workflow.util.ReactiveHelper;
import com.anshuman.statemachinedemo.workflow.util.StringUtil;
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

    @Transactional
    public LeaveAppWorkFlowInstanceEntity createLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity)
        throws Exception {
        validateThatEntityDoesNotExist(entity);
        stateMachineAdapter.persist(stateMachineAdapter.create(LEAVE_APP_WF_V1), entity);
        LeaveAppWorkFlowInstanceEntity savedEntity = leaveAppRepository.save(entity);
        log.debug("saved entity {}", savedEntity);
        return savedEntity;
    }

    public LeaveAppWorkFlowInstanceEntity getLeaveApplicationById(@NotNull Long id) throws Exception {
        Optional<LeaveAppWorkFlowInstanceEntity> entityOpt = leaveAppRepository.findById(id);
        if (entityOpt.isEmpty()) {
            log.warn("No entity found with id: {}", id);
            return null;
        }
        LeaveAppWorkFlowInstanceEntity entity = entityOpt.get();
        log.debug("found entity: {}", entity);
        return entity;
    }

    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull Long id, LeaveAppEvent... events) throws Exception {
        LeaveAppWorkFlowInstanceEntity entity = getLeaveApplicationById(id);
        return updateLeaveApplication(entity, events);
    }

    @Transactional
    public LeaveAppWorkFlowInstanceEntity updateLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent... events) throws Exception {
        passEventsToEntityStateMachine(entity, events);
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

    private void passEventsToEntityStateMachine(LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent... events) throws Exception {
        if (events != null && events.length > 0) {
            StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = getStateMachineFromEntity(entity);
            List<EventResult<LeaveAppState, LeaveAppEvent>> eventResults = ReactiveHelper.stateMachineHandler(stateMachine, events);
            if (!ReactiveHelper.parseResultToBool(eventResults)) {
                String eventStr = eventResults
                    .stream()
                    .filter(EventResult.accepted)
                    .map(StringUtil::event)
                    .collect(Collectors.joining(", "));

                throw new StateMachineException("Did not persist the state machine context to the database, "
                    + "as the following passed events: [" + eventStr + "]" +
                    " were not accepted by the statemachine of the LeaveApp with id: " + entity.getId());
            }
            saveStateMachineToEntity(stateMachine, entity);
        }
    }

    private void saveStateMachineToEntity(@NotNull StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        @NotNull LeaveAppWorkFlowInstanceEntity entity) throws Exception {
        validateThatEntityHasStateMachineContext(entity);
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());
    }

    private StateMachine<LeaveAppState, LeaveAppEvent> getStateMachineFromEntity(LeaveAppWorkFlowInstanceEntity entity) throws Exception {
        validateThatEntityHasStateMachineContext(entity);
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = stateMachineAdapter.restore(LEAVE_APP_WF_V1, entity);
        log.debug("entity id: {}, current state: {}, stateMachine current state: {}", entity.getId(),
            entity.getCurrentState(), stateMachine.getState().getId());
        return stateMachine;
    }

    private void validateThatEntityDoesNotExist(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getId() != null && existsById(entity.getId()))
            throw new RuntimeException("Cannot save LeaveAppWorkflowInstance. An entry with this id already exists");
    }

    private void validateThatEntityHasStateMachineContext(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getStateMachineContext() == null) {
            throw new StateMachineException("No state machine context found for the entity");
        }
    }

}
