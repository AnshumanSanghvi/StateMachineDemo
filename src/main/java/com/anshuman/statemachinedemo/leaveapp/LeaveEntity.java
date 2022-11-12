package com.anshuman.statemachinedemo.leaveapp;

import com.anshuman.statemachinedemo.entity.BaseWorkflowEntity;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineContext;

@Entity
@Getter
@Setter
public class LeaveEntity extends BaseWorkflowEntity<LeaveAppState, LeaveAppEvent, Long> {

    @Override
    public void setStateMachineContext(StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext) {
        super.setCurrentState(stateMachineContext.getState());
        super.setStateMachineContext(stateMachineContext);
    }

}
