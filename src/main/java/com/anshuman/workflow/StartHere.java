package com.anshuman.workflow;

import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.LEAVE_APP_WF_V1;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_FORWARD;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_INITIALIZE;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_SUBMIT;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_COMPLETE;
import static com.anshuman.workflow.statemachine.event.LeaveAppEvent.E_TRIGGER_REVIEW_OF;
import static com.anshuman.workflow.statemachine.util.EventResultHelper.toResultDTOList;
import static com.anshuman.workflow.statemachine.util.EventSendHelper.sendEvent;
import static com.anshuman.workflow.statemachine.util.EventSendHelper.sendForwardEvent;

import com.anshuman.workflow.data.dto.WorkflowTypeDto;
import com.anshuman.workflow.data.model.entity.ContextEntity;
import com.anshuman.workflow.service.WorkflowTypeService;
import com.anshuman.workflow.statemachine.action.LeaveAppActions;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.persist.DefaultStateMachineAdapter;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartHere implements CommandLineRunner {

    private final DefaultStateMachineAdapter<LeaveAppState, LeaveAppEvent, ContextEntity<LeaveAppState, LeaveAppEvent>> stateMachineAdapter;
    private final WorkflowTypeService workflowTypeService;

    @Override
    public void run(String... args) throws Exception {
        //leaveAppStateMachine();
        createWorkflowType();
    }

    public void createWorkflowType() {
        var dto = new WorkflowTypeDto(1L, 1, LocalDateTime.now(),
            null, null, LocalDateTime.now(),
            1, (short) 1, null, false, false,
            true, true, List.of(-1L, 0L),
            3, 3, Map.of(1, 234L, 2, 123L, 3, 235L));
        var wftype = workflowTypeService.createWorkflowType(WorkflowTypeDto.toEntity(dto));
        log.info("workflow type created: {}", wftype);
    }

    public void leaveAppStateMachine() {
        StateMachine<LeaveAppState, LeaveAppEvent> sm = stateMachineAdapter.create(LEAVE_APP_WF_V1);

        int reviewersCount = 3;
        Map<Integer, Long> reviewerMap = new LinkedHashMap<>(reviewersCount);
        reviewerMap.put(1, 234L);
        reviewerMap.put(2, 123L);
        reviewerMap.put(3, 235L);
        boolean isParallel = false;
        int maxChangeRequests = 3;
        int maxRollBackCount = 3;
        LeaveAppActions.StateActions.initial(sm, reviewersCount, reviewerMap, isParallel,
            maxChangeRequests, maxRollBackCount);

        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> resultList = new ArrayList<>();
        resultList.addAll(toResultDTOList(sendEvent(sm, E_INITIALIZE)));
        resultList.addAll(toResultDTOList(sendEvent(sm, E_SUBMIT)));
        resultList.addAll(toResultDTOList(sendEvent(sm, E_TRIGGER_REVIEW_OF)));
        resultList.addAll(toResultDTOList(sendForwardEvent(sm, E_FORWARD, 1, 234L)));
        resultList.addAll(toResultDTOList(sendForwardEvent(sm, E_FORWARD, 2, 123L)));
        resultList.addAll(toResultDTOList(sendForwardEvent(sm, E_FORWARD, 3, 235L)));
        resultList.addAll(toResultDTOList(sendEvent(sm, E_TRIGGER_COMPLETE)));

        log.info("SM Event Results: \n{}", resultList.stream()
            .map(EventResultDTO::toString)
            .collect(Collectors.joining("\n")));

        log.info("Final SM State: {}, ExtendedState: [{}]", sm.getState().getId(),
            sm.getExtendedState().getVariables().entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> (String) entry.getKey()))
                .map(entry -> entry.getKey() + " : " + entry.getValue())
                .collect(Collectors.joining(", ")));
    }

}
