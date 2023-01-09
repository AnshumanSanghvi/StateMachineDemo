package com.anshuman.statemachinedemo.model.dao;

import com.anshuman.statemachinedemo.exception.WorkflowException;
import com.anshuman.statemachinedemo.model.entity.WorkflowEventLogEntity;
import com.anshuman.statemachinedemo.workflow.data.dto.WorkflowEventLogDTO;
import com.anshuman.statemachinedemo.workflow.data.enums.WorkflowType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowEventLogDAO {

    private final JdbcTemplate jdbcTemplate;

    private static final String WF_LOG_TABLE_NAME = "wf_status_log";
    public static final String LEAVE_APP_WF_TABLE_NAME = "leaveapp_wf_status_log";


    public List<WorkflowEventLogEntity> getWorkflowEventLogByType(WorkflowEventLogDTO wf) {
        String query = "SELECT * " +
            " FROM wf_status_log " +
            " WHERE type_id = " + wf.getTypeId().getTypeId() + " " +
            Optional.ofNullable(wf.getCompanyId()).map(cid -> " AND company_id = " + cid).orElse("") +
            Optional.ofNullable(wf.getBranchId()).map(bid -> " AND branch_id = " + bid).orElse("") +
            Optional.ofNullable(wf.getInstanceId()).map(iid -> " AND instance_id = " + iid).orElse("") +
            Optional.ofNullable(wf.getActionDate()).map(ad -> " AND action_date > " + ad).orElse("") +
            Optional.ofNullable(wf.getState()).map(st -> " AND state = " + st).orElse("") +
            Optional.ofNullable(wf.getEvent()).map(e -> " AND event = " + e).orElse("") +
            Optional.ofNullable(wf.getActionBy()).map(by -> " AND action_by = " + by).orElse("") +
            Optional.ofNullable(wf.getUserRole()).map(ur -> " AND user_role = " + ur).orElse("") +
            Optional.ofNullable(wf.getCompleted()).map(c -> " AND completed = " + c).orElse("") +
            " ORDER BY instance_id DESC, action_date DESC";
        final String partitionTableName = WorkflowEventLogType.getTableForWorkflowType(wf.getTypeId());
        final String partitionAwareQuery = query.replace(WF_LOG_TABLE_NAME, partitionTableName);
        try {
            return jdbcTemplate.query(partitionAwareQuery, (rs, rowNum) -> WorkflowEventLogEntity
                .builder()
                .id(rs.getLong("id"))
                .companyId(rs.getLong("company_id"))
                .branchId(rs.getLong("branch_id"))
                .typeId(WorkflowType.fromId(rs.getInt("type_id")))
                .instanceId(rs.getLong("instance_id"))
                .actionDate(rs.getTimestamp("action_date").toLocalDateTime())
                .state(rs.getString("state"))
                .event(rs.getString("event"))
                .actionBy(rs.getLong("action_by"))
                .userRole(rs.getShort("user_role"))
                .completed(rs.getShort("completed"))
                .build());
        } catch (DataAccessException ex) {
            throw new WorkflowException(ex);
        }
    }

}

enum WorkflowEventLogType {
    LEAVE_APP(WorkflowType.LEAVE_APPLICATION, WorkflowEventLogDAO.LEAVE_APP_WF_TABLE_NAME);

    private final WorkflowType workflowType;

    private final String table;

    private static final WorkflowEventLogType[] values  = WorkflowEventLogType.values();

    WorkflowEventLogType(WorkflowType workflowType, String table) {
        this.workflowType = workflowType;
        this.table = table;
    }

    public static String getTableForWorkflowType(WorkflowType type) {
        for(WorkflowEventLogType wfelt : values) {
            if (wfelt.workflowType.equals(type))
                return wfelt.table;
        }
        throw new IllegalArgumentException("Unsupported WorkflowType");
    }

}
