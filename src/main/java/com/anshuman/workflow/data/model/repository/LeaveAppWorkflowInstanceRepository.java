package com.anshuman.workflow.data.model.repository;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.data.model.repository.projection.LAWFProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@SuppressWarnings("NullableProblems")
@Repository
public interface LeaveAppWorkflowInstanceRepository
    extends JpaRepository<LeaveAppWorkFlowInstanceEntity, Long> {

    // TODO: optimize query. wf_type_mst is doing a left outer join on wf_type_dtl
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


    @Query(value = " SELECT new com.anshuman.workflow.data.model.repository.projection.LAWFProjection( "
        + " lawf.id, lawf.currentState, lawf.stateMachineContext, lawf.isActive) "
        + " FROM LeaveAppWorkFlowInstanceEntity lawf "
        + " WHERE lawf.isActive = 1 "
        + "     AND lawf.id = :id")
    Optional<LAWFProjection> findPartialById(@Param("id") Long id);
}
