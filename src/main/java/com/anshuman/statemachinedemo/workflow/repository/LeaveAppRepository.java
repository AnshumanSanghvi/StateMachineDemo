package com.anshuman.statemachinedemo.workflow.repository;

import com.anshuman.statemachinedemo.workflow.entity.LeaveAppEntity;
import com.anshuman.statemachinedemo.workflow.state.LeaveAppState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveAppRepository extends JpaRepository<LeaveAppEntity, Long> {

    Page<LeaveAppEntity> findByCurrentState(LeaveAppState state, Pageable page);

}
