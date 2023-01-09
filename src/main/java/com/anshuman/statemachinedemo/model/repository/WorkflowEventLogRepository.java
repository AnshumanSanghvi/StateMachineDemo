package com.anshuman.statemachinedemo.model.repository;

import com.anshuman.statemachinedemo.model.entity.WorkflowEventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowEventLogRepository extends JpaRepository<WorkflowEventLogEntity, Long> {

}