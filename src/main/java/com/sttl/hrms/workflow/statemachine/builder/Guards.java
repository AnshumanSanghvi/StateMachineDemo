package com.sttl.hrms.workflow.statemachine.builder;


import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity.WorkflowProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;

import java.util.*;

import static com.sttl.hrms.workflow.statemachine.SMConstants.*;
import static com.sttl.hrms.workflow.statemachine.util.ExtStateUtil.get;
import static com.sttl.hrms.workflow.statemachine.util.ExtStateUtil.getStateId;

@Slf4j
@SuppressWarnings("unchecked")
public class Guards {

    private Guards() {
        // use class statically
    }

    private static final WorkflowProperties defaultWFP = new WorkflowProperties();

    // check whether the approval flow is serial or parallel
    public static boolean approvalFlow(StateContext<String, String> context) {
        log.debug("Executing guard: approvalFlowGuard with currentState: {}", getStateId(context));
        String approvalFlow = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL);
        return approvalFlow.equalsIgnoreCase(VAL_PARALLEL);
    }

    public static boolean approveInParallel(StateContext<String, String> context) {
        log.debug("Executing guard: parallelApprovalGuard with currentState: {}", getStateId(context));

        if (adminApprove(context)) return true;

        // check that the approving reviewer is present in the list of reviewers for the application
        Long approvedBy = get(context, KEY_APPROVE_BY, Long.class, null);
        Map<Integer, Long> reviewersMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class,
                Collections.emptyMap());
        return !isUserAbsentFromReviewerList(reviewersMap, approvedBy);
    }

    public static boolean approveInSerial(StateContext<String, String> context) {
        log.debug("Executing guard: serialApprovalGuard with currentState: {}", getStateId(context));

        if (adminApprove(context)) return true;

        int totalReviewers = get(context, KEY_REVIEWERS_COUNT, Integer.class, 0);
        int forwardedTimes = get(context, KEY_FORWARDED_COUNT, Integer.class, 0);

        // check that the number of times the application is forwarded is equal to the total number of reviewers.
        boolean forwardedCountMatchesTotalReviewers = totalReviewers == forwardedTimes;

        // check that all the reviewers have forwarded the application
        Map<Integer, Pair<Long, Boolean>> forwardedMap = (Map<Integer, Pair<Long, Boolean>>) get(context, KEY_FORWARDED_MAP,
                Map.class, Collections.emptyMap());
        boolean allReviewersHaveApproved = forwardedMap.entrySet().stream()
                .allMatch(entry -> entry.getValue().getSecond());

        // check that the last reviewer in the order is the one who forwarded the application
        Map<Integer, Long> reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        Pair<Integer, Long> forwardedBy = (Pair<Integer, Long>) get(context, KEY_LAST_FORWARDED_BY, Pair.class, null);
        boolean forwarderIsFinalReviewer = reviewerMap.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .filter(entry -> forwardedBy.getFirst().equals(entry.getKey()) && forwardedBy.getSecond()
                        .equals(entry.getValue()))
                .isPresent();

        return forwardedCountMatchesTotalReviewers && allReviewersHaveApproved && forwarderIsFinalReviewer;
    }

    private static boolean adminApprove(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        var map = context.getExtendedState().getVariables();
        map.put(KEY_APPROVE_BY, actionBy);
        map.put(KEY_APPROVE_COMMENT, comment);

        Long approvedBy = get(context, KEY_APPROVE_BY, Long.class, null);

        // check that the approving reviewer is valid (i.e. not null or 0)
        if (isUserIdInvalid(approvedBy)) return false;

        var adminList = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        return adminList.contains(approvedBy);
    }

    public static boolean requestChanges(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        var map = context.getExtendedState().getVariables();
        map.put(KEY_CHANGES_REQ_BY, new Pair<>(orderNo, actionBy));
        map.put(KEY_CHANGE_REQ_COMMENT, comment);

        // check that the reviewer requesting changes is valid and belongs to the list of reviewers for the application.
        Pair<Integer, Long> requestChangesBy = get(context, KEY_CHANGES_REQ_BY, Pair.class, null);
        Long reviewerId = requestChangesBy.getSecond();
        var reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (reviewerId == null || reviewerId == 0 || !reviewerMap.containsValue(reviewerId)) {
            log.error("Cannot allow returning the application to the applicant as {}",
                    "the reviewer requesting changes has an invalid id");
            return false;
        }

        // check that the reviwer requesting changes has input a valid comment for the request
        String requestedChangeComment = get(context, KEY_CHANGE_REQ_COMMENT, String.class, null);
        if (requestedChangeComment == null || requestedChangeComment.isBlank()) {
            log.error("Cannot allow returning the application to the applicant as {}",
                    "there is no valid request change comment");
            return false;
        }

        // check that the maximum allowed rollbacks is not 0, and that total number of returns so far don't exceed its max threshold.
        int maxAllowedReturns = get(context, KEY_CHANGE_REQ_MAX, Integer.class, defaultWFP.getChangeReqMaxCount());
        int returnsSoFar = get(context, KEY_RETURN_COUNT, Integer.class, 0);
        if (maxAllowedReturns != -1 && (returnsSoFar + 1 > maxAllowedReturns)) {
            log.error("Cannot allow returning the application to the applicant, as {}",
                    "max return count already reached.");
            return false;
        }

        Pair<Integer, Long> lastForwardedBy = get(context, KEY_LAST_FORWARDED_BY, Pair.class, null);
        Map<Integer, Pair<Long, Boolean>> forwardMap = get(context, KEY_FORWARDED_MAP,
                Map.class, Collections.emptyMap());

        // if valid lastForwardedBy then
        if ((lastForwardedBy != null) && (lastForwardedBy.getSecond() != null) && (lastForwardedBy.getSecond() != 0L)) {
            // first remove all entries in forwardedMap where forwarding hasn't occurred.
            var entrySet = new HashSet<>(forwardMap.entrySet());
            entrySet.removeIf(entry -> !entry.getValue().getSecond());
            for (Map.Entry<Integer, Pair<Long, Boolean>> entry : entrySet) {
                // then check that no one has forwarded the application after the current user
                if (entry.getKey() > orderNo) return false;
            }
        } else // application hasn't been forwarded
            return Objects.equals(actionBy, reviewerMap.get(1));

        return true;
    }

    public static boolean rollBackApproval(StateContext<String, String> context) {
        log.debug("Executing guard: rollBackCountGuard with currentState: {}", getStateId(context));

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        var map = context.getExtendedState().getVariables();
        map.put(KEY_ROLL_BACK_BY, new Pair<>(orderNo, actionBy));
        map.put(KEY_ROLL_BACK_COMMENT, comment);

        // check that userId is valid
        if (isUserIdInvalid(actionBy)) return false;

        // check that roll back is allowed
        int maxRollBack = get(context, KEY_ROLL_BACK_MAX, Integer.class, defaultWFP.getRollbackMaxCount());
        if (isRollBackAllowed(maxRollBack)) return false;

        // check that we have not hit the max rollback limit
        int rollbackCount = get(context, KEY_ROLL_BACK_COUNT, Integer.class, 0);
        if (isRollBackCountOverLimit(maxRollBack, rollbackCount)) return false;

        // check that the user requesting roll back is in the admin list
        var adminList = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        Pair<Integer, Long> rollBackPair = ((Pair<Integer, Long>) get(context, KEY_ROLL_BACK_BY, Pair.class, null));
        Long rollBackBy = rollBackPair.getSecond();
        if (adminList.contains(rollBackBy)) {
            return true;
        }

        // check that the user requesting rollback is in the reviewer list
        var reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (isUserAbsentFromReviewerList(reviewerMap, rollBackBy)) return false;

        // check that for serial approval flow, the user rolling back approval is the latest reviewer who forwarded the application
        boolean isSerial = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerial) {
            Long forwardBy = ((Pair<Integer, Long>) get(context, KEY_LAST_FORWARDED_BY, Pair.class, null)).getSecond();
            if (forwardBy.longValue() != rollBackBy.longValue()) {
                log.error("Cannot roll back the application as the roll back reviewerId: {} does not match the forwarded reviewerId: {}",
                        rollBackBy, forwardBy);
                return false;
            }
        }

        return true;
    }

    public static boolean forward(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        var map = context.getExtendedState().getVariables();
        map.put(KEY_LAST_FORWARDED_BY, new Pair<>(orderNo, actionBy));
        map.put(KEY_FORWARDED_COMMENT, comment);

        // check if admin action
        var adminIds = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        if (adminIds.contains(actionBy)) return true;

        final String errorMsg = "Cannot forward the application as {}";

        if (!reviewCountCheck(context)) return false;

        // check that the reviewer forwarding the application is valid (i.e. not null or 0)
        Pair<Integer, Long> forwardedBy = (Pair<Integer, Long>) get(context, KEY_LAST_FORWARDED_BY, Pair.class, null);
        if (forwardedBy == null || forwardedBy.getSecond() == 0L) {
            log.error(errorMsg, "The forwarding reviewer id is 0 or null");
            return false;
        }

        boolean isParallelFlow = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_PARALLEL);
        Map<Integer, Long> reviewersMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (isParallelFlow) {
            // check that the forwardingId is present in the reviewersMap in the parallel approval flow.
            Long forwardingId = forwardedBy.getSecond();
            if (!reviewersMap.containsValue(forwardingId)) {
                log.error(errorMsg, "the forwarding userId is not in the reviewers list");
                return false;
            }
        } else {
            // check that both the order of forwarding,
            // and that the forwarding user is present in the list of reviewers for the application in the serial approval flow.
            return forwardIdAndOrderCheck(context);
        }

        return true;
    }

    private static boolean reviewCountCheck(StateContext<String, String> context) {
        int forwardedCount = get(context, KEY_FORWARDED_COUNT, Integer.class, 0);
        int reviewerCount = get(context, KEY_REVIEWERS_COUNT, Integer.class, 0);

        // check that the application has reviewers
        if (reviewerCount <= 0) {
            log.error("Cannot forward the application as {}", "there are no reviewers for the application");
            return false;
        }

        // check that the application is not forwarded more times than total reviewers.
        if (forwardedCount + 1 > reviewerCount) {
            log.error("Cannot forward the application as {}", "the application is being forwarded more times than the number of defined reviewers");
            return false;
        }

        return true;
    }

    private static boolean forwardIdAndOrderCheck(StateContext<String, String> context) {
        Map<Integer, Long> reviewersMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        Pair<Integer, Long> forwardedBy = (Pair<Integer, Long>) get(context, KEY_LAST_FORWARDED_BY, Pair.class, null);
        Integer forwardingOrder = forwardedBy.getFirst();
        Long forwardingId = forwardedBy.getSecond();

        // check that the order number and the userId of the forwarding reviewer are present in the list of reviewers for the application.
        boolean isOrderNumberAndReviewerIdAbsent = reviewersMap
                .entrySet()
                .stream()
                .noneMatch(entry -> Objects.equals(entry.getKey(), forwardingOrder) &&
                        Objects.equals(entry.getValue(), forwardingId));
        if (isOrderNumberAndReviewerIdAbsent) {
            log.error("Cannot forward the application as {}", "the combination of the forwarding order " +
                    "and the forwarding userId is not present in the list of reviewers");
            return false;
        }

        Map<Integer, Pair<Long, Boolean>> forwardMap = (Map<Integer, Pair<Long, Boolean>>) get(context, KEY_FORWARDED_MAP,
                Map.class, Collections.emptyMap());
        List<Pair<Long, Boolean>> list = new ArrayList<>(forwardMap.values());
        int upperLimit = list.indexOf(new Pair<>(forwardedBy.getSecond(), false)); // returns -1 if pair is not present.

        // check that the same reviewer hasn't already forwarded this application.
        if (upperLimit < 0) {
            log.error("Cannot forward the application as {}", "no eligible reviewer found to forward the application");
            return false;
        }

        // check that all reviewers before the current one have already forwarded this application
        if (upperLimit > 0) {
            boolean forwardOrderMaintained = list.subList(0, upperLimit).stream().allMatch(Pair::getSecond);
            if (!forwardOrderMaintained) {
                log.error("Cannot forward the application as {}", "previous reviewers have not forwarded the application");
                return false;
            }
        }

        return true;
    }

    /**
     * CHECKS
     **/

    private static boolean isRollBackAllowed(final int maxRollBack) {
        if (maxRollBack <= 0) {
            log.error("Cannot roll back the application as the max roll back count is {}", maxRollBack);
            return true;
        }
        return false;
    }

    private static boolean isRollBackCountOverLimit(final int maxRollBack, final int rollbackCount) {
        if (rollbackCount + 1 > maxRollBack) {
            log.error(" Cannot roll back the application as the roll back count: {} exceeds the max roll back count: {}",
                    rollbackCount + 1, maxRollBack);
            return true;
        }
        return false;
    }

    private static boolean isUserAbsentFromReviewerList(final Map<Integer, Long> reviewerMap, final Long rollBackBy) {
        if (!reviewerMap.containsValue(rollBackBy)) {
            log.error("Cannot perform action on the application as the reviewer id: {} is not present in the reviewer's list: {}",
                    rollBackBy, reviewerMap);
            return true;
        }
        return false;
    }

    private static boolean isUserIdInvalid(Long userId) {
        if (userId == null || userId == 0) {
            log.error("Invalid id of user performing action. userId: " + userId);
            return true;
        }
        return false;
    }
}
