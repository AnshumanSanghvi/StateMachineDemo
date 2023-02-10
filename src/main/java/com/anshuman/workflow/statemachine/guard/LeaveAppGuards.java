package com.anshuman.workflow.statemachine.guard;


import static com.anshuman.workflow.statemachine.data.constant.LeaveAppSMConstants.*;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getInt;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getLong;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getMap;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getPair;
import static com.anshuman.workflow.statemachine.util.ExtendedStateHelper.getString;

import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.event.LeaveAppEvent;
import com.anshuman.workflow.statemachine.state.LeaveAppState;
import com.anshuman.workflow.statemachine.util.ExtendedStateHelper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;

@Slf4j
public class LeaveAppGuards {

    private LeaveAppGuards() {
        // use class statically
    }

    public static boolean rollBackApproval(StateContext<LeaveAppState, LeaveAppEvent> context) {
        log.info("Executing guard: rollBackCountGuard with currentState: {}", context.getStateMachine().getState().getId());

        int maxRollBack = getInt(context, KEY_ROLL_BACK_MAX);
        if (maxRollBack <= 0) {
            log.error("Cannot roll back the application as the max roll back count is {}", maxRollBack);
            return false;
        }

        int rollbackCount = getInt(context, KEY_ROLL_BACK_COUNT);
        if (rollbackCount + 1 > maxRollBack) {
            log.error(" Cannot roll back the application as the roll back count: {} exceeds the max roll back count: {}",
                rollbackCount + 1, maxRollBack);
            return false;
        }

        var reviewerMap = getMap(context, KEY_REVIEWERS_MAP);
        Long rollBackBy = getPair(context, KEY_ROLL_BACK_BY).getSecond();
        if (!reviewerMap.containsValue(rollBackBy)) {
            log.error("Cannot roll back the application as the reviewer id: {} is not present in the reviewersMap: {}",
                rollBackBy, reviewerMap);
        }

        boolean isSerial = getString(context, KEY_APPROVAL_FLOW_TYPE, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerial) {
            Long forwardBy = getPair(context, KEY_LAST_FORWARDED_BY).getSecond();
            if (forwardBy.longValue() != rollBackBy.longValue()) {
                log.error("Cannot roll back the application as the roll back reviewerId: {} does not match the forwarded reviewerId: {}",
                    rollBackBy, forwardBy);
                return false;
            }
        }

        return true;
    }

    public static boolean approvalFlow(StateContext<LeaveAppState, LeaveAppEvent> context) {
        log.info("Executing guard: approvalFlowGuard with currentState: {}", context.getStateMachine().getState().getId());
        String approvalFlow = getString(context, KEY_APPROVAL_FLOW_TYPE, VAL_SERIAL);
        return approvalFlow.equalsIgnoreCase(VAL_PARALLEL);
    }

    public static boolean approveInParallel(StateContext<LeaveAppState, LeaveAppEvent> context, Map<Integer, Long> reviewersMap) {
        log.info("Executing guard: parallelApprovalGuard with currentState: {}", context.getStateMachine().getState().getId());

        var map = context.getExtendedState().getVariables();
        Long approvingUserId = getLong(context, KEY_APPROVE_BY);

        if (approvingUserId == null || approvingUserId == 0) {
            map.remove(KEY_APPROVE_BY);
            log.error("Cannot approve the statemachine as the approving userId is null or 0");
            return false;
        }

        if (!reviewersMap.containsValue(approvingUserId)) {
            map.remove(KEY_APPROVE_BY);
            log.error("Cannot approve the statemachine as the approving userId: {} is {}", approvingUserId, "not present in the reviewer's list");
            return false;
        }

        return true;
    }

    public static boolean requestChanges(StateContext<LeaveAppState, LeaveAppEvent> context) {

        Long reviewerId = getLong(context, KEY_REQUESTED_CHANGES_BY);
        int returnsSoFar = getInt(context, KEY_RETURN_COUNT);
        int maxAllowedReturns = getInt(context, KEY_MAX_CHANGE_REQUESTS, 1000);
        String requestedChangeComment = getString(context, KEY_REQUESTED_CHANGE_COMMENT);
        var reviewerMap = getMap(context, KEY_REVIEWERS_MAP);

        if (reviewerId == null || reviewerId == 0 || !reviewerMap.containsValue(reviewerId)) {
            log.error("Cannot allow returning the application to the applicant as {}",
                "the reviewer requesting changes has an invalid id");
            return false;
        }

        if (requestedChangeComment == null || requestedChangeComment.isBlank()) {
            log.error("Cannot allow returning the application to the applicant as {}",
                "there is no valid request change comment");
            return false;
        }

        if (maxAllowedReturns != -1 && (returnsSoFar + 1 > maxAllowedReturns)) {
            log.error("Cannot allow returning the application to the applicant, as {}",
                "max return count already reached.");
            return false;
        }

        return true;
    }

    public static boolean forward(StateContext<LeaveAppState, LeaveAppEvent> context) {

        int forwardedCount = getInt(context, KEY_FORWARDED_COUNT);
        int reviewerCount = getInt(context, KEY_REVIEWERS_COUNT);

        if (reviewerCount <= 0) {
            log.error("Cannot forward the application as {}", "there are no reviewers for the application");
            return false;
        }

        if (forwardedCount + 1 > reviewerCount) {
            log.error("Cannot forward the application as {}", "the application is being forwarded more times than the number of defined reviewers");
            return false;
        }

        Pair<Integer, Long> forwardedBy = getPair(context, KEY_LAST_FORWARDED_BY);

        if (forwardedBy == null || forwardedBy.getSecond() == 0L) {
            log.error("Cannot forward the application as {}", "The forwarding reviewer id is 0 or null");
            return false;
        }

        Integer forwardingOrder = forwardedBy.getFirst();
        Long forwardingId = forwardedBy.getSecond();

        boolean isParallelFlow = getString(context, KEY_APPROVAL_FLOW_TYPE, VAL_SERIAL)
            .equalsIgnoreCase(VAL_PARALLEL);

        Map<Integer, Long> reviewersMap = getMap(context, KEY_REVIEWERS_MAP);
        if (isParallelFlow) {
            // only need to check that the forwardingId is present in the reviewersMap in the parallel approval flow.
            if (!reviewersMap.containsValue(forwardingId)) {
                log.error("Cannot forward the application as {}", "the forwarding userId is not in the reviewers list");
            }
        } else {
            // need to check both the order of forwarding, and the forwardingId are present
            // in the reviewersMap in the serial approval flow.
            boolean isOrderNumberAndReviewerIdAbsent = reviewersMap
                .entrySet()
                .stream()
                .noneMatch(entry -> Objects.equals(entry.getKey(), forwardingOrder) &&
                    Objects.equals(entry.getValue(), forwardingId));
            if (isOrderNumberAndReviewerIdAbsent) {
                log.error("Cannot forward the application as {}", "the combination of the forwarding order and the forwarding "
                    + "userId is not present in the list of reviewers");
                return false;
            }

            Map<Integer, Pair<Long, Boolean>> forwardMap = getMap(context, KEY_FORWARDED_MAP);
            LinkedList<Pair<Long, Boolean>> list = new LinkedList<>(forwardMap.values());

            int upperLimit = list.indexOf(new Pair<>(forwardedBy.getSecond(), false));

            if (upperLimit < 0) {
                log.error("Cannot forward the application as {}", "no eligible reviewer found to forward the application");
                return false;
            }

            if (upperLimit > 0) {

                boolean forwardOrderMaintained = list.subList(0, upperLimit)
                    .stream()
                    .allMatch(Pair::getSecond);

                if (!forwardOrderMaintained) {
                    log.error("Cannot forward the application as {}", "previous reviewers have not forwarded the application");
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean approve(StateContext<LeaveAppState, LeaveAppEvent> context) {
        int totalReviewers = ExtendedStateHelper.getInt(context, KEY_REVIEWERS_COUNT);
        int forwardedTimes = ExtendedStateHelper.getInt(context, KEY_FORWARDED_COUNT);
        boolean forwardedCountMatchesTotalReviewers = totalReviewers == forwardedTimes;

        Map<Integer, Pair<Long, Boolean>> forwardedMap = ExtendedStateHelper.getMap(context, KEY_FORWARDED_MAP);
        boolean allReviewersHaveApproved = forwardedMap.entrySet().stream().allMatch(entry -> entry.getValue().getSecond());

        Map<Integer, Long> reviewerMap = ExtendedStateHelper.getMap(context, KEY_REVIEWERS_MAP);
        Pair<Integer, Long> forwardedBy = ExtendedStateHelper.getPair(context, KEY_LAST_FORWARDED_BY);
        Map.Entry<Integer, Long> finalReviewer = new ArrayList<>(reviewerMap.entrySet()).get(Math.max(totalReviewers - 1, 0));
        boolean forwarderIsFinalReviewer = new Pair<>(finalReviewer.getKey(), finalReviewer.getValue()).equals(forwardedBy);

        return forwardedCountMatchesTotalReviewers && allReviewersHaveApproved && forwarderIsFinalReviewer;
    }

}
