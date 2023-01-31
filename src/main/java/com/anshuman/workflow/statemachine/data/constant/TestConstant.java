package com.anshuman.workflow.statemachine.data.constant;


public class TestConstant {


    private TestConstant() {
        // use class statically
    }

    public static final String STATE_MACHINE_NAME = "TestStateMachine";

    // ExtendedState Keys
    public static final String KEY_ANY_APPROVE = "ANY_APPROVE";
    public static final String KEY_APPROVAL_FLOW_TYPE = "APPROVAL_FLOW_TYPE";
    public static final String KEY_APPROVE_BY = "APPROVE_BY";
    public static final String KEY_CLOSED_STATE_TYPE = "CLOSED_STATE_TYPE";
    public static final String KEY_FORWARDED_BY = "FORWARDED_BY";
    public static final String KEY_FORWARDED_COUNT = "FORWARDED_COUNT";
    public static final String KEY_MAX_CHANGE_REQUESTS = "MAX_CHANGE_REQUESTS";
    public static final String KEY_REQUESTED_CHANGES_BY = "REQUESTED_CHANGES_BY";
    public static final String KEY_REQUESTED_CHANGE_COMMENT = "REQUESTED_CHANGE";
    public static final String KEY_RETURN_COUNT = "RETURN_COUNT";
    public static final String KEY_REVIEWERS_COUNT = "REVIEWERS_COUNT";
    public static final String KEY_REVIEWERS_MAP = "REVIEWERS_MAP";
    public static final String KEY_ROLL_BACK_BY = "ROLL_BACK_BY";
    public static final String KEY_ROLL_BACK_COUNT = "ROLL_BACK_COUNT";
    public static final String KEY_ROLL_BACK_MAX = "ROLL_BACK_MAX";
    public static final String KEY_FORWARDED_MAP = "FORWARDED_MAP";

    // ExtendedState Values
    public static final String VAL_APPROVED = "APPROVED";
    public static final String VAL_SERIAL = "SERIAL";
    public static final String VAL_PARALLEL = "PARALLEL";
    public static final String VAL_REJECTED = "REJECTED";
    public static final String VAL_CANCELED = "CANCELED";

    // Transactions
    public static final String TX_SYSTEM_TRIGGERS_APPROVAL_FLOW_JUNCTION = "SystemTriggersTheApprovalFlowJunction";
    public static final String TX_USER_CANCELS_LEAVE_APP_UNDER_REVIEW = "UserCancelsTheLeaveApplicationUnderReview";
    public static final String TX_USER_CANCELS_LEAVE_APP_IN_PARALLEL_FLOW = "UserCancelsTheLeaveApplicationUnderReviewInParallelFlow";
    public static final String TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_PARALLEL_FLOW = "ReviewerRequestsChangesInTheLeaveApplicationUnderReview";
    public static final String TX_REVIEWER_REJECTS_LEAVE_APP_IN_PARALLEL_FLOW = "ReviewerRejectsTheLeaveApplicationUnderReview";
    public static final String TX_REVIEWER_APPROVES_LEAVE_APPLICATION_IN_PARALLEL_FLOW = "ReviewerApprovesTheLeaveApplicationUnderReview";
    public static final String TX_USER_CREATES_LEAVE_APP = "UserCreatesTheLeaveApplication";
    public static final String TX_USER_SUBMITS_LEAVE_APP = "UserSubmitsTheCreatedLeaveApplication";
    public static final String TX_SYSTEM_TRIGGERS_LEAVE_APP_FOR_REVIEW = "SystemTriggersTheSubmittedLeaveApplication";
    public static final String TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW = "ReviewerRollsBackTheLeaveApplicationUnderReview";
    public static final String TX_USER_CANCELS_LEAVE_APP_IN_SERIAL_FLOW = "UserCancelsTheLeaveApplicationUnderReview";
    public static final String TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_SERIAL_FLOW = "ReviewerRequestsChangesInTheLeaveApplicationUnderReview";
    public static final String TX_REVIEWER_REJECTS_LEAVE_APP_IN_SERIAL_FLOW = "ReviewerRejectsTheLeaveApplicationUnderReview";
    public static final String TX_REVIEWER_APPROVES_LEAVE_APP_IN_SERIAL_FLOW = "ReviewerApprovesTheLeaveApplicationUnderReview";
    public static final String TX_SYSTEM_COMPLETES_LEAVE_APP = "SystemCompletesTheLeaveApplication";
    public static final String TX_REVIEWER_FORWARDS_APPLICATION = "ReviewerForwardsApplication";

}
