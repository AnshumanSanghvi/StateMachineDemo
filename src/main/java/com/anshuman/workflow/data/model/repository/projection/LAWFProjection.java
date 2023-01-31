package com.anshuman.workflow.data.model.repository.projection;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.statemachine.StateMachineContext;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class LAWFProjection {
    private Long id;
    private LeaveAppState currentState;
    private StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext;
    private short isActive;

    public LAWFProjection(Long id, LeaveAppState currentState, StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext) {
        this.id = id;
        this.currentState = currentState;
        this.stateMachineContext = stateMachineContext;
        this.isActive = 1;
    }

    public static LeaveAppWorkFlowInstanceEntity toEntity(LAWFProjection projection) {
        LeaveAppWorkFlowInstanceEntity entity = new LeaveAppWorkFlowInstanceEntity();
        entity.setId(projection.getId());
        entity.setCurrentState(projection.getCurrentState());
        entity.setStateMachineContext(projection.getStateMachineContext());
        entity.setIsActive(projection.getIsActive());
        return entity;
    }



}
