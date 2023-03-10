package com.anshuman.workflow.data.model.repository;

import com.anshuman.workflow.data.model.entity.LoanAppWorkflowInstanceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanAppWorkflowInstanceRepository extends JpaRepository<LoanAppWorkflowInstanceEntity, Long> {

    @Query("SELECT true "
        + "FROM LoanAppWorkflowInstanceEntity lawf "
        + "INNER JOIN WorkflowTypeEntity wft "
        + "     ON wft.typeId = lawf.typeId "
        + "WHERE wft.isActive = 1 "
        + "     AND lawf.isActive = 1 "
        + "     AND lawf.id = :id")
    Boolean existsByIdAndWFType(@Param("id") Long id);

    @Query("SELECT lawf "
        + "FROM LoanAppWorkflowInstanceEntity lawf "
        + "INNER JOIN WorkflowTypeEntity wft "
        + "     ON wft.typeId = lawf.typeId "
        + "WHERE wft.isActive = 1 "
        + "     AND lawf.isActive = 1 "
        + "     AND lawf.id = :id")
    Optional<LoanAppWorkflowInstanceEntity> findById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE LoanAppWorkflowInstanceEntity lwf "
        + "SET lwf.isActive = 0, "
        + "lwf.deletedDate = NOW()"
        + "WHERE lwf.id = :id")
    void deleteById(@Param("id") Long id);


    @Query(value = " SELECT lawf "
        + " FROM LoanAppWorkflowInstanceEntity lawf "
        + " WHERE lawf.isActive = 1 ")
    List<LoanAppWorkflowInstanceEntity> findAll();
}