package com.anshuman.statemachinedemo.workflow.model.entity;

import com.anshuman.statemachinedemo.workflow.model.ContextEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.event.LeaveAppEvent;
import com.anshuman.statemachinedemo.workflow.model.enums.state.LeaveAppState;
import com.anshuman.statemachinedemo.workflow.persist.StateMachineContextConverter;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Where;
import org.springframework.statemachine.StateMachineContext;

@Entity
@Table(name = "leave_wf_inst", schema = "public")
@Where(clause = "isActive = 1")
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
public class LeaveAppWorkFlowInstanceEntity extends WorkflowInstanceEntity
    implements ContextEntity<LeaveAppState, LeaveAppEvent> {

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(name = "current_state")
    private LeaveAppState currentState;

    @Convert(converter = StateMachineContextConverter.class)
    @Column(name = "statemachine", columnDefinition = "bytea")
    @ToString.Exclude
    private StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext;

    @Column(name = "is_active")
    private short isActive = 1;

    public void setStateMachineContext(StateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext) {
        this.setCurrentState(stateMachineContext.getState());
        this.stateMachineContext = stateMachineContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        WorkflowTypeEntity that = (WorkflowTypeEntity) o;
        return super.getId() != null && Objects.equals(super.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
