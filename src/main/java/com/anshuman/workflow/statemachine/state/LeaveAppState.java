package com.anshuman.workflow.statemachine.state;

public enum LeaveAppState {
    S_INITIAL,
    S_CREATED,
    S_SUBMITTED,
    S_UNDER_PROCESS,
    S_CLOSED,
    S_COMPLETED,
    S_APPROVAL_JUNCTION,
    S_SERIAL_APPROVAL_FLOW,
    S_PARALLEL_APPROVAL_FLOW
}
