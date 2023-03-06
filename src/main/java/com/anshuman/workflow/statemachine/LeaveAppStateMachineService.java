package com.anshuman.workflow.statemachine;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.data.model.repository.WorkflowTypeRepository;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.resource.dto.WorkflowEventLogDto;
import com.anshuman.workflow.service.WorkflowEventLogService;
import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.persist.DefaultStateMachineAdapter;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import com.anshuman.workflow.statemachine.util.EventResultHelper;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import com.anshuman.workflow.statemachine.util.StringUtil;
import com.anshuman.workflow.statemachine.util.WFPropsToSMExtStateHelper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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
// TODO: generalize this class for all state machines
public class LeaveAppStateMachineService {

    private final DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, LeaveAppWorkFlowInstanceEntity> stateMachineAdapter;
    private final WorkflowTypeRepository workflowTypeRepository;
    private final WorkflowEventLogService workflowEventLogService;

    public StateMachine<LeaveAppState, LeaveAppEvent> createStateMachine(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        // create statemachine as per the entity's statemachine id.
        var stateMachine = stateMachineAdapter.create(entity.getStateMachineId());

        if (stateMachine == null) {
            throw new StateMachineException("StateMachine was not created");
        }

        // set the state machine extended state from the workflow type and workflow instance
        List<Pair<Integer, Long>> reviewers = entity.getReviewers();
        var properties = getWorkFlowPropertiesByType(entity.getTypeId());
        WFPropsToSMExtStateHelper.setExtendedStateProperties(stateMachine, properties, reviewers);

        return stateMachine;
    }

    public Pair<StateMachine<LeaveAppState, LeaveAppEvent>, List<EventResultDTO<LeaveAppState, LeaveAppEvent>>> passEventsToStateMachine(Long entityId,
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        PassEventDto eventDto) {

        LeaveAppEvent event = LeaveAppEvent.getByName(eventDto.getEvent());
        Long actionBy = eventDto.getActionBy();
        Integer order = eventDto.getOrderNo();
        String comment = eventDto.getComment();

        // send the event to the state machine
        var resultFlux = switch (event) {
            case E_INITIALIZE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION,
                E_APPROVE, E_REJECT, E_CANCEL, E_TRIGGER_COMPLETE -> EventSendHelper.sendEvent(stateMachine, event);
            case E_REQUEST_CHANGES_IN -> EventSendHelper.sendRequestChangesEvent(stateMachine, event, order, actionBy, comment);
            case E_FORWARD -> EventSendHelper.sendForwardEvent(stateMachine, event, order, actionBy);
            case E_ROLL_BACK -> EventSendHelper.sendRollBackApprovalEvent(stateMachine, event, order, actionBy);
        };
        log.debug("After passing event: {}, resultFlux is: {}", event, resultFlux);

        // parse the result
        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> resultDTOList = EventResultHelper.toResultDTOList(resultFlux);

        // find out if any event wasn't accepted.
        boolean hasErrors = resultDTOList.stream().anyMatch(Predicate.not(EventResultDTO.accepted));

        // throw error if the event is not accepted by the state machine.
        if (hasErrors) {
            String eventStr = resultDTOList
                .stream()
                .filter(Predicate.not(EventResultDTO.accepted))
                .map(StringUtil::event)
                .collect(Collectors.joining(", "));

            log.error("Did not persist the state machine context to the database, "
                + "as the following passed event: [" + eventStr + "]" +
                " were not accepted by the statemachine of the LeaveApp with id: " + entityId);

            return new Pair<>(stateMachine, Collections.emptyList());
        }

        return new Pair<>(stateMachine, resultDTOList);
    }

    @Transactional
    public void saveStateMachineToEntity(@NotNull StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        @NotNull LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent event, boolean validate) {
        if (validate) validateThatEntityHasStateMachineContext(entity);
        // TODO: FIX: this is where event is still initialize
        stateMachine.stopReactively().block();
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());

        // log the event asynchronously once it is successfully processed by the statemachine.
        writeToLog(entity, stateMachine, event);
    }

    private static void validateThatEntityHasStateMachineContext(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getStateMachineContext() == null) {
            throw new StateMachineException("No state machine context found for the entity");
        }
    }

    public StateMachine<LeaveAppState, LeaveAppEvent> getStateMachineFromEntity(LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityHasStateMachineContext(entity);
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = stateMachineAdapter.restore(LEAVE_APP_WF_V1, entity);
        log.debug("For entity with id: {} and currentState: {}, Restored statemachine with id: {} and current state: {}",
            entity.getId(), entity.getCurrentState(), stateMachine.getId(), stateMachine.getState().getId());
        return stateMachine;
    }

    public void writeToLog(LeaveAppWorkFlowInstanceEntity entity, StateMachine<LeaveAppState, LeaveAppEvent> stateMachine, LeaveAppEvent event) {
        // log the event asynchronously once it is successfully processed by the statemachine.
        var wfEventLogDto = WorkflowEventLogDto.builder()
            .companyId(entity.getCompanyId())
            .branchId(entity.getBranchId())
            .typeId(entity.getTypeId())
            .instanceId(entity.getId())
            .state(stateMachine.getState().getId().toString())
            .event(Optional.ofNullable(event).map(LeaveAppEvent::name).orElse(null))
            .actionDate(LocalDateTime.now())
            .completed((short) (stateMachine.isComplete() ? 1 : 0))
            .actionBy(entity.getCreatedByUserId())
            .userRole((short) 0)
            .build();
        workflowEventLogService.logEvent(wfEventLogDto);
    }

    public WorkflowProperties getWorkFlowPropertiesByType(WorkflowType workflowType) {
        var properties = workflowTypeRepository.getPropertiesByTypeId(workflowType);
        log.debug("Retrieved workflow properties: {} from workflowType: {}", properties, workflowType);
        return  properties;
    }

}
