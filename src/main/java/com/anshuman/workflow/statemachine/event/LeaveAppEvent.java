package com.anshuman.workflow.statemachine.event;

import lombok.Getter;

public enum LeaveAppEvent {
    E_INITIALIZE("Initialize Leave Application"),
    E_SUBMIT("Submit Leave Application"),
    E_TRIGGER_REVIEW_OF("Request Review of Leave Application"),
    E_REQUEST_CHANGES_IN("Request Changes to Submitted Leave Application"),
    E_TRIGGER_FLOW_JUNCTION("Transition to the approval flow type junction"),
    E_FORWARD("Forward Leave Application to the next Approver"),
    E_APPROVE("Approve Leave Application"),
    E_REJECT("Reject Leave Application"),
    E_CANCEL("Cancel Leave Application"),
    E_ROLL_BACK("Roll Back Decision on Leave Application"),
    E_TRIGGER_COMPLETE("Close Leave Application");

    @Getter
    private final String humanReadableStatus;

    private static final LeaveAppEvent[] values = LeaveAppEvent.values();

    LeaveAppEvent(String humanReadableStatus) {
        this.humanReadableStatus = humanReadableStatus;
    }

    public static LeaveAppEvent getByName(String name) {
        for(LeaveAppEvent e : values) {
            if(e.name().equalsIgnoreCase(name))
                return e;
        }
        throw new IllegalArgumentException("No event with the given name found");
    }

}
