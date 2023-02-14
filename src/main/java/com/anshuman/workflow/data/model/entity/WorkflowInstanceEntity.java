package com.anshuman.workflow.data.model.entity;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.converter.WorkflowTypeIdConverter;
import java.util.Objects;
import javax.persistence.*;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        WorkflowInstanceEntity that = (WorkflowInstanceEntity) o;
        return this.getId() != null && Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
