package com.anshuman.statemachinedemo.workflow.entity;


import com.anshuman.statemachinedemo.workflow.model.ContextEntity;
import com.anshuman.statemachinedemo.workflow.persist.StateMachineContextConverter;
import java.io.Serializable;
import javax.persistence.Column;
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

public class BaseWorkflowEntity<S, E, I extends Serializable> implements ContextEntity<S, E, I> {

    @Id
    @Setter
    @Column(nullable = false, unique = true, updatable = false)
    private I id;

    @Convert(converter = StateMachineContextConverter.class)
    @Column(name = "statemachine", columnDefinition = "bytea")
    private StateMachineContext<S, E> stateMachineContext;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(name = "current_state")
    private S currentState;

    public void setStateMachineContext(StateMachineContext<S, E> context) {
        this.setCurrentState(context.getState());
        this.stateMachineContext = context;
    }
}
