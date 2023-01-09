package com.anshuman.statemachinedemo.model.repository.projection;

import com.anshuman.statemachinedemo.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
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
    private short isActive = 1;


    public static LeaveAppWorkFlowInstanceEntity toEntity(LAWFProjection projection) {
        LeaveAppWorkFlowInstanceEntity entity = new LeaveAppWorkFlowInstanceEntity();
        entity.setId(projection.getId());
        entity.setCurrentState(projection.getCurrentState());
        entity.setStateMachineContext(projection.getStateMachineContext());
        entity.setIsActive(projection.getIsActive());
        return entity;
    }



}
