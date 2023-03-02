package com.anshuman.workflow.data.model.repository;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowProperties;
import com.anshuman.workflow.data.model.entity.WorkflowTypeEntity;
import java.util.Optional;
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

    @Query("SELECT wf.workflowProperties "
        + "FROM WorkflowTypeEntity wf "
        + "WHERE wf.typeId = :workflowType "
        + "     AND wf.isActive = 1")
    WorkflowProperties getPropertiesByTypeId(@Param("workflowType") WorkflowType workflowType);

    @Modifying
    @Query("UPDATE WorkflowTypeEntity wft "
        + "SET wft.isActive = 0, "
        + "wft.deletedDate = NOW()"
        + "WHERE wft.typeId = :type")
    void deleteByTypeId(@Param("type") WorkflowType type);

    @Query("SELECT wf "
        + "FROM WorkflowTypeEntity wf "
        + "WHERE wf.isActive = 1 "
        + "     AND wf.typeId = :type ")
    Optional<WorkflowTypeEntity> findByTypeId(WorkflowType type);

}
