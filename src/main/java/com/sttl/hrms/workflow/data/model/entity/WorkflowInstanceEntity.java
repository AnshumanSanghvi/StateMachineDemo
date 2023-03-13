package com.sttl.hrms.workflow.data.model.entity;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.data.model.converter.WorkflowTypeIdConverter;
import com.sttl.hrms.workflow.statemachine.data.Pair;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "wf_inst_mst", schema = "public")
@Inheritance(strategy = InheritanceType.JOINED)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public abstract class WorkflowInstanceEntity extends BaseEntity {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WF_INST_SEQ")
    @SequenceGenerator(name = "WF_INST_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "type_id", nullable = false, updatable = false)
    @Convert(converter = WorkflowTypeIdConverter.class)
    private WorkflowType typeId;

    @Column(name = "create_by")
    private Long createdByUserId;

    @Column(name = "update_by")
    private Long updatedByUserId;

    @Column(name = "delete_by")
    private Long deletedByUserId;

    @Column(name = "roll_back_count")
    private short timesRolledBackCount;

    @Column(name = "return_count")
    private short timesReturnedCount;

    @Column(name = "version")
    private short workflowVersion;

    @Column(columnDefinition = "jsonb", name = "reviewers")
    @Type(type = "jsonb")
    @Basic(fetch = FetchType.EAGER)
    private List<Pair<Integer, Long>> reviewers = Collections.emptyList();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        if (!(o instanceof WorkflowInstanceEntity that)) {
            return false;
        }
        if (getTimesRolledBackCount() != that.getTimesRolledBackCount()) {
            return false;
        }
        if (getTimesReturnedCount() != that.getTimesReturnedCount()) {
            return false;
        }
        if (getWorkflowVersion() != that.getWorkflowVersion()) {
            return false;
        }
        if (getId() != null && !getId().equals(that.getId())) {
            return false;
        }
        if (getCreatedByUserId() != null && !getCreatedByUserId().equals(that.getCreatedByUserId())) {
            return false;
        }
        if (getUpdatedByUserId() != null && !getUpdatedByUserId().equals(that.getUpdatedByUserId())) {
            return false;
        }
        if (getDeletedByUserId() != null && !getDeletedByUserId().equals(that.getDeletedByUserId())) {
            return false;
        }
        if (getReviewers() != null && !getReviewers().equals(that.getReviewers())) {
            return false;
        }

        return getTypeId() != that.getTypeId();
    }

    @Override
    public int hashCode() {
        int result = getTypeId().hashCode();
        if (getId() != null) result = 31 * result + getId().hashCode();
        if(getCreatedByUserId() != null) result = 31 * result + getCreatedByUserId().hashCode();
        if(getUpdatedByUserId() != null) result = 31 * result + getUpdatedByUserId().hashCode();
        if(getDeletedByUserId() != null) result = 31 * result + getDeletedByUserId().hashCode();
        result = 31 * result + (int) getTimesRolledBackCount();
        result = 31 * result + (int) getTimesReturnedCount();
        result = 31 * result + (int) getWorkflowVersion();
        result = 31 * result + getReviewers().hashCode();
        return result;
    }
}
