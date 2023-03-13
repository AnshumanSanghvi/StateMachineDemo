package com.sttl.hrms.workflow.data.model.entity;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.statemachine.data.Pair;
import java.util.List;
import org.springframework.statemachine.support.DefaultStateMachineContext;


public interface ContextEntity<S, E> {

    Long getCompanyId();

    Integer getBranchId();

    Long getId();

    WorkflowType getTypeId();

    String getStateMachineId();

    S getCurrentState();

    DefaultStateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(DefaultStateMachineContext<S, E> context);

    List<Pair<Integer, Long>> getReviewers();

}