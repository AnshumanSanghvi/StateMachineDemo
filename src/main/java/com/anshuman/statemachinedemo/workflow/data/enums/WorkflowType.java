package com.anshuman.statemachinedemo.workflow.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    LEAVE_APPLICATION(1, "LeaveApplicationWorkflow");

    private final int typeId;

    private final String name;

    private static final WorkflowType[] WORKFLOW_TYPES = WorkflowType.values();

    public static WorkflowType fromId(int typeId) {
        for(WorkflowType workflowType : WORKFLOW_TYPES) {
            if (workflowType.getTypeId() == typeId) {
                return workflowType;
            }
        }
        throw new IllegalArgumentException("No workflowType found for given id: " + typeId);
    }

    public static WorkflowType fromName(String name) {
        for(WorkflowType workflowType : WORKFLOW_TYPES) {
            if (workflowType.getName().equalsIgnoreCase(name)) {
                return workflowType;
            }
        }
        throw new IllegalArgumentException("No workflowType found for given id: " + name);
    }

}