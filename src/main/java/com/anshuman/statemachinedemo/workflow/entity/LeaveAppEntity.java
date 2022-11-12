package com.anshuman.statemachinedemo.workflow.entity;

import com.anshuman.statemachinedemo.workflow.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineContext;

@Entity
@Getter
@Setter
public class LeaveAppEntity extends BaseWorkflowEntity<LeaveAppState, LeaveAppEvent, Long> {

    @Override
    public void setStateMachineContext(StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext) {
        super.setCurrentState(stateMachineContext.getState());
        super.setStateMachineContext(stateMachineContext);
    }

}
