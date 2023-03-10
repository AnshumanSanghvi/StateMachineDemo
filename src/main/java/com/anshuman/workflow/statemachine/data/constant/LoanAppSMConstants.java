package com.anshuman.workflow.statemachine.data.constant;

public class LoanAppSMConstants {
    private LoanAppSMConstants() {
        // use class statically
    }

    public static final String LOAN_APP_WF_V1 = "LoanApplicationWorkflowStateMachineV1";

    // ExtendedState Keys
    public static final String KEY_ANY_APPROVE = "ANY_APPROVE";
    public static final String KEY_APPROVAL_FLOW_TYPE = "APPROVAL_FLOW_TYPE";
    public static final String KEY_APPROVE_BY = "APPROVE_BY";
    public static final String KEY_CLOSED_STATE_TYPE = "CLOSED_STATE_TYPE";
    public static final String KEY_FORWARDED_COUNT = "FORWARDED_COUNT";
    public static final String KEY_FORWARDED_MAP = "FORWARDED_MAP";
    public static final String KEY_LAST_FORWARDED_BY = "LAST_FORWARDED_BY";
    public static final String KEY_MAX_CHANGE_REQUESTS = "MAX_CHANGE_REQUESTS";
    public static final String KEY_REQUESTED_CHANGES_BY = "REQUESTED_CHANGES_BY";
    public static final String KEY_REQUESTED_CHANGE_COMMENT = "REQUESTED_CHANGE";
    public static final String KEY_FORWARDED_COMMENT = "FORWARDED_COMMENT";
    public static final String KEY_REVIEWERS_COUNT = "REVIEWERS_COUNT";
    public static final String KEY_REVIEWERS_MAP = "REVIEWERS_MAP";
    public static final String KEY_ROLL_BACK_BY = "ROLL_BACK_BY";
    public static final String KEY_ROLL_BACK_MAX = "ROLL_BACK_MAX";

    // ExtendedState Values
    public static final String VAL_APPROVED = "APPROVED";
    public static final String VAL_CANCELED = "CANCELED";
    public static final String VAL_PARALLEL = "PARALLEL";
    public static final String VAL_REJECTED = "REJECTED";
    public static final String VAL_SERIAL = "SERIAL";

    // Transactions
    public static final String TX_REVIEWER_APPROVES_LOAN_APPLICATION_IN_PARALLEL_FLOW = "ReviewerApprovesTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_APPROVES_LOAN_APP_IN_SERIAL_FLOW = "ReviewerApprovesTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_FORWARDS_APPLICATION = "ReviewerForwardsApplication";
    public static final String TX_REVIEWER_REJECTS_LOAN_APP_IN_PARALLEL_FLOW = "ReviewerRejectsTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_REJECTS_LOAN_APP_IN_SERIAL_FLOW = "ReviewerRejectsTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_PARALLEL_FLOW = "ReviewerRequestsChangesInTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_REQUESTS_CHANGES_FROM_USER_IN_SERIAL_FLOW = "ReviewerRequestsChangesInTheLoanApplicationUnderReview";
    public static final String TX_REVIEWER_ROLLS_BACK_APPROVAL_IN_SERIAL_FLOW = "ReviewerRollsBackTheLoanApplicationUnderReview";
    public static final String TX_SYSTEM_COMPLETES_LOAN_APP = "SystemCompletesTheLoanApplication";
    public static final String TX_SYSTEM_TRIGGERS_APPROVAL_FLOW_JUNCTION = "SystemTriggersTheApprovalFlowJunction";
    public static final String TX_SYSTEM_TRIGGERS_LOAN_APP_FOR_REVIEW = "SystemTriggersTheSubmittedLoanApplication";
    public static final String TX_USER_CANCELS_CREATED_LOAN_APP = "UserCancelsTheSubmittedLoanApplication";
    public static final String TX_USER_CANCELS_LOAN_APP_IN_PARALLEL_FLOW = "UserCancelsTheLoanApplicationUnderReviewInParallelFlow";
    public static final String TX_USER_CANCELS_LOAN_APP_IN_SERIAL_FLOW = "UserCancelsTheLoanApplicationUnderReview";
    public static final String TX_USER_CANCELS_LOAN_APP_UNDER_REVIEW = "UserCancelsTheLoanApplicationUnderReview";
    public static final String TX_USER_CREATES_LOAN_APP = "UserCreatesTheLoanApplication";
    public static final String TX_USER_SUBMITS_LOAN_APP = "UserSubmitsTheCreatedLoanApplication";

}
