package com.anshuman.workflow.data.model.repository;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@SuppressWarnings("NullableProblems")
@Repository
public interface WorkflowTypeRepository extends JpaRepository<WorkflowTypeEntity, Long> {

    @Query("SELECT true "
        + "FROM WorkflowTypeEntity wf "
        + "WHERE wf.typeId = :workflowType "
        + "     AND wf.isActive = 1")
    Boolean existsByTypeId(@Param("workflowType") WorkflowType workflowType);

    @Modifying
    @Query("UPDATE WorkflowTypeEntity wft "
        + "SET wft.isActive = 0 "
        + "WHERE wft.id = :id")
    void deleteById(@Param("id") Long id);

}
