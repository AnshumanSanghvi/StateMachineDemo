package com.anshuman.workflow.statemachine;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;

import com.anshuman.workflow.data.dto.WorkflowEventLogDto;
import com.anshuman.workflow.data.enums.WorkFlowTypeStateMachine;
import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.WorkflowTypeRepository;
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
import java.util.List;
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

    public void saveStateMachineForCreatedLeaveApplication(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        // get the state machine associated with the given workflow type as per required state machine version.
        var stateMachine = getStateMachineFromWFType(entity.getTypeId(), "v1");

        // set the state machine extended state from the workflow type and workflow instance
        List<Pair<Integer, Long>> reviewers = entity.getReviewers();
        var properties = workflowTypeRepository.getPropertiesByTypeId(entity.getTypeId());
        WFPropsToSMExtStateHelper.setExtendedStateProperties(stateMachine, properties, reviewers);

        // save the state machine
        saveStateMachineToEntity(stateMachine, entity);
    }

    public void passEventsToEntityStateMachine(LeaveAppWorkFlowInstanceEntity entity, LeaveAppEvent event) {

        // get the state machine for the given entity.
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = getStateMachineFromEntity(entity);

        // send the event to the state machine
        var resultFlux = EventSendHelper.sendEvent(stateMachine, event);

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

            throw new StateMachineException("Did not persist the state machine context to the database, "
                + "as the following passed event: [" + eventStr + "]" +
                " were not accepted by the statemachine of the LeaveApp with id: " + entity.getId());
        }

        // save the state machine context once event is accepted.
        saveStateMachineToEntity(stateMachine, entity);

        // log the event asynchronously once it is successfully processed by the statemachine.
        var wfEventLogDto = WorkflowEventLogDto.builder()
            .companyId(entity.getCompanyId())
            .branchId(entity.getBranchId())
            .typeId(entity.getTypeId())
            .instanceId(entity.getId())
            .state(stateMachine.getState().getId().toString())
            .event(event.toString())
            .actionDate(LocalDateTime.now())
            .completed((short) (stateMachine.isComplete() ? 1 : 0))
            .actionBy(0L)
            .userRole((short) 0)
            .build();
        workflowEventLogService.logEvent(wfEventLogDto);
    }

    private void saveStateMachineToEntity(@NotNull StateMachine<LeaveAppState, LeaveAppEvent> stateMachine,
        @NotNull LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityHasStateMachineContext(entity);
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());
    }

    private static void validateThatEntityHasStateMachineContext(@NotNull LeaveAppWorkFlowInstanceEntity entity) {
        if (entity.getStateMachineContext() == null) {
            throw new StateMachineException("No state machine context found for the entity");
        }
    }

    private StateMachine<LeaveAppState, LeaveAppEvent> getStateMachineFromEntity(LeaveAppWorkFlowInstanceEntity entity) {
        validateThatEntityHasStateMachineContext(entity);
        StateMachine<LeaveAppState, LeaveAppEvent> stateMachine = stateMachineAdapter.restore(LEAVE_APP_WF_V1, entity);
        log.debug("entity id: {}, current state: {}, stateMachine current state: {}", entity.getId(),
            entity.getCurrentState(), stateMachine.getState().getId());
        return stateMachine;
    }

    private StateMachine<LeaveAppState, LeaveAppEvent> getStateMachineFromWFType(WorkflowType workflowType, String version) {
        // get the state machine associated with the given workflow type as per required state machine version.
        return WorkFlowTypeStateMachine.getStateMachineIds(workflowType)
            .stream()
            .filter(pair -> pair.getFirst().equalsIgnoreCase(version))
            .findFirst()
            .map(Pair::getSecond)
            .map(stateMachineAdapter::create)
            .orElse(null);
    }

}
