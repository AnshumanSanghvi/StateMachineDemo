package com.anshuman.workflow.statemachine.event;

public enum TestEvent {
    E_INITIALIZE,
    E_SUBMIT,
    E_TRIGGER_REVIEW,
    E_REQUEST_CHANGES,
    E_TRIGGER_FLOW_JUNCTION,
    E_APPROVE,
    E_FORWARD,
    E_REJECT,
    E_CANCEL,
    E_ROLL_BACK,
    E_TRIGGER_COMPLETE
}
