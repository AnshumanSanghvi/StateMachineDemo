package com.anshuman.statemachinedemo.workflow.event;

import lombok.Getter;

public enum LeaveAppEvent {
    START("Initialize Leave Application"),
    SUBMIT("Submit Leave Application"),
    TRIGGER_REVIEW_OF("Request Review of Leave Application"),
    REQUEST_CHANGES_IN("Request Changes to Submitted Leave Application"),
    APPROVE("Approve Leave Application"),
    REJECT("Reject Leave Application"),
    CANCEL("Cancel Leave Application"),
    ROLL_BACK("Roll Back Decision on Leave Application"),
    TRIGGER_COMPLETE("Close Leave Application");

    @Getter
    private final String humanReadableStatus;

    LeaveAppEvent(String humanReadableStatus) {
        this.humanReadableStatus = humanReadableStatus;
    }

}
