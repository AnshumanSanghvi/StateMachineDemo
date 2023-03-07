package com.anshuman.workflow.statemachine;


import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.ContextEntity;
import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.data.model.repository.WorkflowTypeRepository;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.resource.dto.WorkflowEventLogDto;
import com.anshuman.workflow.service.WorkflowEventLogService;
import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import com.anshuman.workflow.statemachine.persist.DefaultStateMachineAdapter;
import com.anshuman.workflow.statemachine.util.StringUtil;
import com.anshuman.workflow.statemachine.util.WFPropsToSMExtStateHelper;
import java.time.LocalDateTime;
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
public class StateMachineService<S, E, T extends ContextEntity<S, E>> {

    private final DefaultStateMachineAdapter<S, E, T> stateMachineAdapter;
    private final WorkflowTypeRepository workflowTypeRepository;
    private final WorkflowEventLogService workflowEventLogService;

    @Transactional(readOnly = true)
    public StateMachine<S, E> createStateMachine(@NotNull T entity) {
        // create statemachine as per the entity's statemachine id.
        var stateMachine = Optional.ofNullable(stateMachineAdapter.create(entity.getStateMachineId()))
            .orElseThrow(() -> new StateMachineException("StateMachine was not created"));

        // set the state machine extended state from the workflow type and workflow instance
        List<Pair<Integer, Long>> reviewers = entity.getReviewers();
        var properties = getWorkFlowPropertiesByType(entity.getTypeId());
        WFPropsToSMExtStateHelper.setExtendedStateProperties(stateMachine, properties, reviewers);

        return stateMachine;
    }

    @Transactional
    public void saveStateMachineToEntity(@NotNull StateMachine<S, E> stateMachine, @NotNull T entity, PassEventDto eventDto) {
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());

        // log the event asynchronously once it is successfully processed by the statemachine.
        writeToLog(entity, stateMachine, eventDto);
    }

    @Transactional(readOnly = true)
    public StateMachine<S, E> getStateMachineFromEntity(T entity) {
        StateMachine<S, E> stateMachine = stateMachineAdapter.restore(entity.getStateMachineId(), entity);
        log.debug("For entity with id: {} and currentState: {}, Restored statemachine: {}",
            entity.getId(), entity.getCurrentState(), StringUtil.stateMachine(stateMachine, false));
        return stateMachine;
    }

    public void writeToLog(T entity, StateMachine<S, E> stateMachine, PassEventDto eventDto) {
        // log the event asynchronously once it is successfully processed by the statemachine.
        var wfEventLogDto = WorkflowEventLogDto.builder()
            .companyId(entity.getCompanyId())
            .branchId(entity.getBranchId())
            .typeId(entity.getTypeId())
            .instanceId(entity.getId())
            .state(stateMachine.getState().getId().toString())
            .event(eventDto.getEvent())
            .actionDate(LocalDateTime.now())
            .completed((short) (stateMachine.isComplete() ? 1 : 0))
            .actionBy(eventDto.getActionBy())
            .userRole((short) 0)
            .comment(eventDto.getComment())
            .build();
        workflowEventLogService.logEvent(wfEventLogDto);
    }

    @Transactional(readOnly = true)
    public WorkflowProperties getWorkFlowPropertiesByType(WorkflowType workflowType) {
        var properties = workflowTypeRepository.getPropertiesByTypeId(workflowType);
        log.debug("Retrieved workflow properties: {} from workflowType: {}", properties, workflowType);
        return properties;
    }

}
