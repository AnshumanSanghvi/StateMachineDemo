package com.anshuman.statemachinedemo.workflow.model.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Slf4j
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long companyId;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private Long branchId;

    @Column(name = "create_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "update_date")
    private LocalDateTime updatedDate;

    @Column(name = "delete_date")
    private LocalDateTime deletedDate;


    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdDate == null)
            this.createdDate = now;
        if (this.updatedDate == null)
            this.updatedDate = now;
        log.debug("{} entity: {}", "Saving", this);
    }

    @PreRemove
    public void preRemove() {
        this.deletedDate = LocalDateTime.now();
        log.debug("{} entity: {}", "Deleting", this);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
        log.debug("{} entity: {}", "Updating", this);
    }

}
