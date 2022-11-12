package com.anshuman.statemachinedemo.entity;

import com.anshuman.statemachinedemo.leaveapp.LeaveAppState;
import com.anshuman.statemachinedemo.pesist.StateMachineContextConverter;
import com.anshuman.statemachinedemo.workflows.ContextEntity;
import java.io.Serializable;
import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.statemachine.StateMachineContext;

@MappedSuperclass
@Getter
@Setter
public class BaseWorkflowEntity<S, E, I extends Serializable> implements ContextEntity<S, E, I> {

    @Id
    private I id;

    @Convert(converter = StateMachineContextConverter.class)
    private StateMachineContext<S, E> stateMachineContext;

    @Enumerated(EnumType.STRING)
    private LeaveAppState currentState;
}
