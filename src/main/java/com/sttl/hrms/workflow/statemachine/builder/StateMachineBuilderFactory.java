package com.sttl.hrms.workflow.statemachine.builder;

import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity.WorkflowProperties;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import org.springframework.statemachine.StateMachine;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sttl.hrms.workflow.statemachine.SMConstants.LEAVE_APP_WF_V1;
import static com.sttl.hrms.workflow.statemachine.SMConstants.LOAN_APP_WF_V1;
import static java.util.stream.Collectors.toMap;

public class StateMachineBuilderFactory {

    private StateMachineBuilderFactory() {
    }

    public static StateMachine<String, String> getStateMachineFromEntityAndType(WorkflowTypeEntity typeEntity,
            List<Pair<Integer, Long>> reviewers) {
        try {
            WorkflowProperties wfProps = typeEntity.getWorkflowProperties();
            return switch (typeEntity.getTypeId()) {
                case LEAVE_APPLICATION ->
                        StateMachineBuilder.createStateMachine(LEAVE_APP_WF_V1,  listToMap(reviewers), wfProps);
                case LOAN_APPLICATION ->
                        StateMachineBuilder.createStateMachine(LOAN_APP_WF_V1, listToMap(reviewers), wfProps);
            };
        } catch (Exception ex) {
            throw new StateMachineException(ex);
        }
    }

    private static Map<Integer, Long> listToMap(List<Pair<Integer, Long>> list) {
        return Optional.ofNullable(list)
                .orElse(Collections.emptyList())
                .stream()
                .collect(toMap(Pair::getFirst,Pair::getSecond));
    }
}
