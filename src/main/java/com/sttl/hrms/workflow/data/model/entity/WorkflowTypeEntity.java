package com.sttl.hrms.workflow.data.model.entity;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.data.model.converter.WorkflowTypeIdConverter;
import com.sttl.hrms.workflow.data.model.converter.WorkflowTypeNameConverter;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "wf_type_mst", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class WorkflowTypeEntity extends BaseEntity {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WF_TYPE_SEQ")
    @SequenceGenerator(name = "WF_TYPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "`name`", nullable = false, length = 64)
    @Convert(converter = WorkflowTypeNameConverter.class)
    private WorkflowType name;

    @Column(name = "wef_date", nullable = false, updatable = false)
    private LocalDateTime withEffectFromDate;

    @Column(name = "type_id", nullable = false, updatable = false)
    @Convert(converter = WorkflowTypeIdConverter.class)
    private WorkflowType typeId;

    @Column(name = "is_active")
    private short isActive;

    @Column(name = "update_by")
    private Long updateByUserId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "properties")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    private WorkflowProperties workflowProperties;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        WorkflowTypeEntity that = (WorkflowTypeEntity) o;
        return this.getId() != null && Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
