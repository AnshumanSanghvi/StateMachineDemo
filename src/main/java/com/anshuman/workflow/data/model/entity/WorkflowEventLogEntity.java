package com.anshuman.workflow.data.model.entity;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.converter.WorkflowTypeIdConverter;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Entity
@Table(name = "wf_status_log", schema = "public")
@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class WorkflowEventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WF_LOG_SEQ")
    // Equivalent of: CREATE SEQUENCE IF NOT EXISTS WF_LOG_SEQ AS BIGINT INCREMENT BY 1 CACHE 50 NO CYCLE OWNED BY wf_status_log.id
    @SequenceGenerator(name = "WF_LOG_SEQ", allocationSize = 1)
    @Column(nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long companyId;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private Integer branchId;

    @Column(name = "type_id", nullable = false, updatable = false)
    @Convert(converter = WorkflowTypeIdConverter.class)
    private WorkflowType typeId;

    @Column(name = "instance_id", nullable = false, updatable = false)
    private Long instanceId;

    @Column(name = "state", nullable = false, updatable = false, length = 100)
    private String state;

    @Column(name = "event", nullable = false, updatable = false, length = 100)
    private String event;

    @Column(name = "action_date", nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(name = "action_by", nullable = false, updatable = false)
    private Long actionBy;

    @Column(name = "user_role", nullable = false, updatable = false)
    private short userRole;

    @Column(name = "completed", nullable = false, updatable = false)
    private short completed;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        WorkflowEventLogEntity that = (WorkflowEventLogEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
