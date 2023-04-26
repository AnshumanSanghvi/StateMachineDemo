package com.sttl.hrms.workflow.statemachine.persist;


import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.data.model.entity.WorkflowInstanceEntity;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity;
import com.sttl.hrms.workflow.data.model.repository.WorkflowTypeRepository;
import com.sttl.hrms.workflow.resource.dto.WorkflowEventLogDto;
import com.sttl.hrms.workflow.service.WorkflowEventLogService;
import com.sttl.hrms.workflow.statemachine.Actions;
import com.sttl.hrms.workflow.statemachine.EventResultDto;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import com.sttl.hrms.workflow.statemachine.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StateMachineService<T extends WorkflowInstanceEntity> {

    private final DefaultStateMachineAdapter<T> stateMachineAdapter;
    private final WorkflowTypeRepository workflowTypeRepository;
    private final WorkflowEventLogService workflowEventLogService;

    @Transactional(readOnly = true)
    public StateMachine<String, String> createStateMachine(@NotNull T entity) {

        // create statemachine as per the entity's statemachine id.
        var stateMachine = Optional
                .ofNullable(stateMachineAdapter.createStateMachine(entity.getStateMachineId()))
                .orElseThrow(() -> new StateMachineException("StateMachine was not created"));

        // set the state machine extended state from the workflow type and workflow instance
        List<Pair<Integer, Long>> reviewersList = entity.getReviewers();
        var properties = getWorkFlowPropertiesByType(entity.getTypeId());
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(Pair.pairListToMap(reviewersList));
        Actions.initial(stateMachine, properties, reviewerMap.size(), reviewerMap, null, null, null);
        return stateMachine;
    }

    @Transactional
    public void saveStateMachineToEntity(@NotNull StateMachine<String, String> stateMachine, @NotNull T entity, List<EventResultDto> eventResultList, boolean logWorkflowEvent) {
        stateMachineAdapter.persist(stateMachine, entity);
        log.debug("persisted stateMachine context: {} for entity with Id: {}", entity.getStateMachineContext(), entity.getId());

        if (logWorkflowEvent)
            writeToLog(entity, stateMachine, eventResultList);
    }

    @Transactional(readOnly = true)
    public StateMachine<String, String> getStateMachineFromEntity(T entity) {
        String stateMachineId = entity.getStateMachineId();
        StateMachine<String, String> stateMachine = stateMachineAdapter.restore(stateMachineAdapter.createStateMachine(stateMachineId), entity);
        log.debug("For entity with id: {} and currentState: {}, Restored statemachine: {}",
                entity.getId(), entity.getCurrentState(), StringUtil.stateMachine(stateMachine, false));
        return stateMachine;
    }

    public void writeToLog(T entity, StateMachine<String, String> stateMachine, List<EventResultDto> eventResultList) {
        for(EventResultDto result : eventResultList) {
            // log the event asynchronously once it is successfully processed by the statemachine.
            var wfEventLogDto = WorkflowEventLogDto.builder()
                    .companyId(entity.getCompanyId())
                    .branchId(entity.getBranchId())
                    .typeId(entity.getTypeId().getTypeId())
                    .instanceId(entity.getId())
                    .state(result.getCurrentState())
                    .event(result.getEvent())
                    .actionDate(LocalDateTime.now())
                    .completed((short) (stateMachine.isComplete() ? 1 : 0))
                    .actionBy(result.getActionBy())
                    .userRole((short) 0)
                    .comment(result.getComment())
                    .build();
            workflowEventLogService.logEvent(wfEventLogDto);
        }
    }

    @Transactional(readOnly = true)
    public WorkflowTypeEntity.WorkflowProperties getWorkFlowPropertiesByType(WorkflowType workflowType) {
        try {
            var properties = workflowTypeRepository.getPropertiesByTypeId(workflowType);
            log.debug("Retrieved workflow properties: {} from workflowType: {}", properties, workflowType);
            return properties;
        } catch (Exception ex) {
            log.warn("Exception in retrieving workflow properties for workflowType: {} with errorMessage: ",
                    workflowType, ex);
            log.info("{}", "returning default workflow type properties");
            return new WorkflowTypeEntity.WorkflowProperties();
        }


    }

}
