package com.anshuman.statemachinedemo.workflow.repository;

import com.anshuman.statemachinedemo.workflow.model.entity.WorkflowTypeEntity;
import com.anshuman.statemachinedemo.workflow.model.enums.WorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
