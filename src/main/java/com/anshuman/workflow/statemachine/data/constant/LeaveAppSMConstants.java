package com.anshuman.workflow.statemachine.data.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LeaveAppSMConstants {

    public static final String LEAVE_APP_WF_V1 = "LeaveApplicationWorkflowStateMachineV1";

    // extended state
    public static final String IS_PARALLEL = "isParallel";
    public static final String ROLL_BACK_COUNT = "rollBackCount";
    public static final String RETURN_COUNT = "returnCount";
    public static final String ONLY_FORWARD_WITH_APPROVAL = "canOnlyForwardWithApproval";
    public static final String CLOSED_STATE = "closedState";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String CANCELED = "canceled";
    public static final String FORWARDED_COUNT = "forwardedCount";
    public static final String REVIEWERS_COUNT = "reviewersCount";
    public static final String APPROVAL_FLOW = "approvalFlow";

    // transitions
    public static final String COMPLETE_LEAVE_APP = "SystemCompletesTheLeaveApplication";
    public static final String ROLL_BACK_LEAVE_APP = "ReviewerRollsBackTheLeaveApplicationUnderReview";
    public static final String REJECT_LEAVE_APP = "ReviewerRejectsTheLeaveApplicationUnderReview";
    public static final String APPROVE_LEAVE_APP = "ReviewerApprovesTheLeaveApplicationUnderReview";
    public static final String CANCEL_LEAVE_APP = "UserCancelsTheLeaveApplicationUnderReview";
    public static final String REQUEST_CHANGES_IN_LEAVE_APP = "ReviewerRequestsChangesInTheLeaveApplicationUnderReview";
    public static final String TRIGGER_LEAVE_APP_REVIEW = "SystemTriggersTheSubmittedLeaveApplication";
    public static final String SUBMIT_CREATED_LEAVE_APP = "UserSubmitsTheCreatedTheLeaveApplication";
    public static final String CREATE_LEAVE_APP = "UserCreatesTheLeaveApplication";


}
