package com.anshuman.statemachinedemo.workflow.repository;

import com.anshuman.statemachinedemo.workflow.model.entity.LeaveAppWorkFlowInstanceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveAppWorkflowInstanceRepository
    extends JpaRepository<LeaveAppWorkFlowInstanceEntity, Long> {

    @Query("SELECT true "
        + "FROM LeaveAppWorkFlowInstanceEntity lawf "
        + "INNER JOIN WorkflowTypeEntity wft "
        + "     ON wft.typeId = lawf.typeId "
        + "WHERE wft.isActive = 1 "
        + "     AND lawf.isActive = 1 "
        + "     AND lawf.id = :id")
    Boolean existsByIdAndWFType(@Param("id") Long id);

    @Query("SELECT lawf "
        + "FROM LeaveAppWorkFlowInstanceEntity lawf "
        + "INNER JOIN WorkflowTypeEntity wft "
        + "     ON wft.typeId = lawf.typeId "
        + "WHERE wft.isActive = 1 "
        + "     AND lawf.isActive = 1 "
        + "     AND lawf.id = :id")
    Optional<LeaveAppWorkFlowInstanceEntity> findById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE LeaveAppWorkFlowInstanceEntity lwf "
        + "SET lwf.isActive = 0 "
        + "WHERE lwf.id = :id")
    void deleteById(@Param("id") Long id);
}
