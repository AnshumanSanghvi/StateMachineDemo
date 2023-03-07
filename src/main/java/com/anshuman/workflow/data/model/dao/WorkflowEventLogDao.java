package com.anshuman.workflow.data.model.dao;

import com.anshuman.workflow.data.enums.WorkflowType;
import com.anshuman.workflow.data.model.entity.WorkflowEventLogEntity;
import com.anshuman.workflow.exception.WorkflowException;
import com.anshuman.workflow.resource.dto.WorkflowEventLogDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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
public class WorkflowEventLogDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String WF_LOG_TABLE_NAME = "wf_status_log";
    public static final String LEAVE_APP_WF_TABLE_NAME = "leaveapp_wf_status_log";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<WorkflowEventLogEntity> getWorkflowEventLogs(WorkflowEventLogDto wf) {
        String query = "SELECT * " +
            " FROM wf_status_log " +
            " WHERE type_id = " + wf.getTypeId() + " " +
            Optional.ofNullable(wf.getCompanyId()).map(cid -> " AND company_id = " + cid).orElse("") +
            Optional.ofNullable(wf.getBranchId()).map(bid -> " AND branch_id = " + bid).orElse("") +
            Optional.ofNullable(wf.getInstanceId()).map(iid -> " AND instance_id = " + iid).orElse("") +
            Optional.ofNullable(wf.getActionDate()).map(DTF::format).map(ad -> " AND action_date > '" + ad + "'::date" ).orElse("") +
            Optional.ofNullable(wf.getState()).map(st -> " AND state = " + st).orElse("") +
            Optional.ofNullable(wf.getEvent()).map(e -> " AND event = " + e).orElse("") +
            Optional.ofNullable(wf.getActionBy()).map(by -> " AND action_by = " + by).orElse("") +
            Optional.ofNullable(wf.getUserRole()).map(ur -> " AND user_role = " + ur).orElse("") +
            Optional.ofNullable(wf.getCompleted()).map(c -> " AND completed = " + c).orElse("") +
            " ORDER BY instance_id DESC, action_date DESC";
        final String partitionTableName = WorkflowEventLogType.getTableForWorkflowType(WorkflowType.fromId(wf.getTypeId()));
        final String partitionAwareQuery = query.replace(WF_LOG_TABLE_NAME, partitionTableName);
        log.debug("workflow event log query: {}", partitionAwareQuery);
        try {
            return jdbcTemplate.query(partitionAwareQuery, (rs, rowNum) -> mapWorkflowEventLogEntityFromResultSet(rs));
        } catch (DataAccessException ex) {
            throw new WorkflowException(ex);
        }
    }

    private static WorkflowEventLogEntity mapWorkflowEventLogEntityFromResultSet(ResultSet rs) throws SQLException {
        return WorkflowEventLogEntity
            .builder()
            .id(rs.getLong("id"))
            .companyId(rs.getLong("company_id"))
            .branchId(rs.getInt("branch_id"))
            .typeId(WorkflowType.fromId(rs.getInt("type_id")))
            .instanceId(rs.getLong("instance_id"))
            .actionDate(rs.getTimestamp("action_date").toLocalDateTime())
            .state(rs.getString("state"))
            .event(rs.getString("event"))
            .actionBy(rs.getLong("action_by"))
            .userRole(rs.getShort("user_role"))
            .completed(rs.getShort("completed"))
            .build();
    }

}

enum WorkflowEventLogType {
    LEAVE_APP(WorkflowType.LEAVE_APPLICATION, WorkflowEventLogDao.LEAVE_APP_WF_TABLE_NAME);

    private final WorkflowType workflowType;

    private final String table;

    private static final WorkflowEventLogType[] values = WorkflowEventLogType.values();

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
