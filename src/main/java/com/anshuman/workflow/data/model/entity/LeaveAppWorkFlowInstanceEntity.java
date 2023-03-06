package com.anshuman.workflow.data.model.entity;

import com.anshuman.workflow.data.enums.LeaveType;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.persist.StateMachineContextConverter;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
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
import org.springframework.statemachine.support.DefaultStateMachineContext;

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
    @Column(name = "current_state", length = 100)
    private LeaveAppState currentState = LeaveAppState.S_INITIAL;

    @Convert(converter = StateMachineContextConverter.class)
    @Column(name = "statemachine", columnDefinition = "bytea")
    @ToString.Exclude
    private DefaultStateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext;

    @Column(name = "is_active", nullable = false)
    private short isActive = 1;

    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "statemachine_id", nullable = false, length = 100)
    private String stateMachineId;

    public void setStateMachineContext(DefaultStateMachineContext<LeaveAppState, LeaveAppEvent> stateMachineContext) {
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
        if (!(o instanceof LeaveAppWorkFlowInstanceEntity that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (getIsActive() != that.getIsActive()) {
            return false;
        }
        if (getCurrentState() != null && (getCurrentState() != that.getCurrentState())) {
            return false;
        }
        if(getStateMachineContext() != null && (!getStateMachineContext().equals(that.getStateMachineContext()))) {
            return false;
        }
        if (getLeaveType() != that.getLeaveType()) {
            return false;
        }
        return getStateMachineId().equals(that.getStateMachineId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getCurrentState().hashCode();
        if (getStateMachineContext() != null) result = 31 * result + getStateMachineContext().hashCode();
        result = 31 * result + (int) getIsActive();
        result = 31 * result + getLeaveType().hashCode();
        result = 31 * result + getStateMachineId().hashCode();
        return result;
    }
}
