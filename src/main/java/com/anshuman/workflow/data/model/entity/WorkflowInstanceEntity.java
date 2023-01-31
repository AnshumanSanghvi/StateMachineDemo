package com.anshuman.workflow.data.model.entity;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.converter.WorkflowTypeIdConverter;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

@Entity
@Table(name = "wf_inst_mst", schema = "public")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public abstract class WorkflowInstanceEntity extends BaseEntity {

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
    private int timesRolledBackCount;

    @Column(name = "return_count")
    private int timesReturnedCount;

    @Column(name = "version")
    private int workflowVersion;

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
