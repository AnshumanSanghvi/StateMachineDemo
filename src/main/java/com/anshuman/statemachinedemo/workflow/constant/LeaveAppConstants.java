package com.anshuman.statemachinedemo.workflow.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LeaveAppConstants {

    public static final String IS_PARALLEL = "isParallel";

    public static final String ROLL_BACK_COUNT = "rollBackCount";

    public static final String RETURN_COUNT = "returnCount";

    public static final String ONLY_FORWARD_WITH_APPROVAL = "canOnlyForwardWithApproval";

    public static final String CLOSED_STATE = "closedState";

    public static final String APPROVED = "approved";

    public static final String REJECTED = "rejected";

    public static final String CANCELED = "canceled";

    public static final String REVIEWED_COUNT = "reviewedCount";
    public static final String REVIEWERS_COUNT = "reviewersCount";
}
