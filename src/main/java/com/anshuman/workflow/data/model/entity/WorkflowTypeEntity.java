package com.anshuman.workflow.data.model.entity;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.converter.WorkflowTypeIdConverter;
import com.anshuman.workflow.data.model.converter.WorkflowTypeNameConverter;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "wf_type_mst", schema = "public")
@SecondaryTable(name = "wf_type_dtl", schema = "public", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
@Where(clause = "isActive = 1")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class WorkflowTypeEntity extends BaseEntity {

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

    @Column(name = "repeat_apprvrs", table = "wf_type_dtl")
    private boolean hasRepeatableApprovers;

    @Column(name = "max_chng_req_limit", table = "wf_type_dtl")
    private int maximumChangeRequestThreshold;

    @Column(name = "can_admin_apprv", table = "wf_type_dtl")
    private boolean canAdminApproveWorkflow;

    @Column(name = "admin_apprv_role_id", table = "wf_type_dtl")
    private Long adminRoleId;

    @Column(name = "total_approvers", table = "wf_type_dtl")
    private int totalApprovers;

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
