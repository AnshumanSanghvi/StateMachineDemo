package com.sttl.hrms.workflow.statemachine.builder;


import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.model.entity.WorkflowTypeEntity.WorkflowProperties;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;

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
        return !isUserAbsentFromUserList(context.getStateMachine(), reviewersMap.values(), approvedBy, "reviewer",
                "approve");
    }

    public static boolean approveInSerial(StateContext<String, String> context) {
        log.debug("Executing guard: serialApprovalGuard with currentState: {}", getStateId(context));

        if (adminApprove(context)) return true;

        // check that the number of times the application is forwarded is equal to the total number of reviewers.
        int totalReviewers = get(context, KEY_REVIEWERS_COUNT, Integer.class, 0);
        int forwardedTimes = get(context, KEY_FORWARDED_COUNT, Integer.class, 0);
        boolean forwardedCountMatchesTotalReviewers = totalReviewers == forwardedTimes;
        if (!forwardedCountMatchesTotalReviewers) return false;

        // check that all the reviewers have forwarded the application
        Map<Integer, Pair<Long, Boolean>> forwardedMap = (Map<Integer, Pair<Long, Boolean>>) get(context, KEY_FORWARDED_MAP,
                Map.class, Collections.emptyMap());
        boolean allReviewersHaveApproved = forwardedMap.entrySet().stream()
                .allMatch(entry -> entry.getValue().getSecond());
        if (!allReviewersHaveApproved) return false;

        // check that the last reviewer in the order is the one who forwarded the application
        Pair<Integer, Long> forwardedBy = (Pair<Integer, Long>) get(context, KEY_FORWARDED_BY_LAST, Pair.class, null);
        if (forwardedBy != null && forwardedBy.getFirst() != null && forwardedBy.getSecond() != null) {
            Map<Integer, Long> reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
            return reviewerMap.entrySet().stream()
                    .max(Map.Entry.comparingByKey())
                    .filter(entry -> forwardedBy.getFirst().equals(entry.getKey()) && forwardedBy.getSecond()
                            .equals(entry.getValue()))
                    .isPresent();
        }

        return true;
    }

    private static boolean adminApprove(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);

        // check that the approving admin is valid (i.e. not null or 0)
        if (isUserIdInvalid(context.getStateMachine(), actionBy, "admin-approve")) return false;

        // check that the admin user id is in admin list
        var adminList = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        if (isUserAbsentFromUserList(context.getStateMachine(), adminList, actionBy, "admin", "adminApprove")) return false;

        return true;
    }

    public static boolean requestChanges(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        // check that the reviewer requesting changes is valid
        if (isUserIdInvalid(context.getStateMachine(), actionBy, "requestChanges")) return false;

        // check that the reviewer belongs to the list of reviewers for the application.
        var reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (isUserAbsentFromUserList(context.getStateMachine(), reviewerMap.values(), actionBy, "reviewers",
                "requestChanges"))
            return false;

        // check that the reviwer requesting changes has input a valid comment for the request
        String requestedChangeComment = get(context, KEY_CHANGE_REQ_COMMENT, String.class, null);
        if (isCommentInvalid(context.getStateMachine(), requestedChangeComment, "requestChanges")) return false;

        // check that the maximum allowed rollbacks is not 0, and that total number of returns so far don't exceed its max threshold.
        int maxAllowedReturns = get(context, KEY_CHANGE_REQ_MAX, Integer.class, defaultWFP.getChangeReqMaxCount());
        int returnsSoFar = get(context, KEY_RETURN_COUNT, Integer.class, 0);
        if (isCountExceedingThreshold(context.getStateMachine(), returnsSoFar, maxAllowedReturns, "Change Requests", "requestChanges"))
            return false;

        Pair<Integer, Long> lastForwardedBy = get(context, KEY_FORWARDED_BY_LAST, Pair.class, null);

        boolean isSerialFlow = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerialFlow) {
            // if the application has been forwarded before:
            if (lastForwardedBy != null && lastForwardedBy.getSecond() != null) {
                // check that the current user is the last person to have forwarded the application,
                // i.e., no one has forwarded the application after the current user.
                Map<Integer, Pair<Long, Boolean>> forwardMap = get(context, KEY_FORWARDED_MAP, Map.class,
                        Collections.emptyMap());
                var entrySet = new HashSet<>(forwardMap.entrySet());
                entrySet.removeIf(entry -> !entry.getValue()
                        .getSecond()); // (remove all successfully forwarded entries)
                for (var entry : entrySet) {
                    // (check if any orderNo is higher than the current user for the remaining entries.)
                    if (entry.getKey() > orderNo) {
                        String errorMsg =
                                "Guard failed for requestChanges as the reviewer: " + actionBy + " at position: " + orderNo + " cannot request" +
                                " changes in the application as there are other reviewers who have already forwarded the application after them";
                        context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                        return false;
                    }
                }
            } else { // if the application has not been forwarded before:
                // check that the current user is the first order user in the reviewersMap
                if (!reviewerMap.get(1).equals(actionBy)) {
                    String errorMsg =
                            "Guard failed for requestChanges as the reviewer: " + actionBy + " at position: " + orderNo +
                                    " cannot request changes in the application as they are not present in the reviewer list";
                    context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean rollBackApproval(StateContext<String, String> context) {
        log.debug("Executing guard: rollBackCountGuard with currentState: {}", getStateId(context));

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        // check that userId is valid
        if (isUserIdInvalid(context.getStateMachine(), actionBy, "rollBack")) return false;

        // check that we have not hit the max rollback limit
        int rollbackCount = get(context, KEY_ROLL_BACK_COUNT, Integer.class, 0);
        int maxRollBack = get(context, KEY_ROLL_BACK_MAX, Integer.class, defaultWFP.getRollbackMaxCount());
        if (isCountExceedingThreshold(context.getStateMachine(), rollbackCount, maxRollBack, "rollBackMax", "rollBack"))
            return false;

        // check that the user requesting roll back is in the admin list
        var adminList = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        if (adminList.contains(actionBy)) {
            return true;
        }

        // check that the user requesting rollback is in the reviewer list
        var reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (isUserAbsentFromUserList(context.getStateMachine(), reviewerMap.values(), actionBy, "reviewers",
                "rollBack"))
            return false;

        // check that for serial approval flow, the user rolling back approval is the latest reviewer who forwarded the application
        boolean isSerial = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerial) {
            Pair<Integer, Long> forwardBy = ((Pair<Integer, Long>) get(context, KEY_FORWARDED_BY_LAST, Pair.class,
                    null));
            if (forwardBy != null && forwardBy.getSecond() != null) {
                if (!forwardBy.getSecond().equals(actionBy)) {
                    String errorMsg = "Cannot roll back the application as the roll back reviewerId: " + actionBy +
                            " does not match the forwarded reviewerId: " + forwardBy;
                    context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean forward(StateContext<String, String> context) {

        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);

        var adminIds = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());

        // check if admin action, then always allow.
        if (adminIds.contains(actionBy)) return true;

        // check if the application is forwarded more times than the number of reviewers.
        if (!reviewCountCheck(context)) return false;

        // check that the reviewer forwarding the application is valid (i.e. not null or 0)
        if (isUserIdInvalid(context.getStateMachine(), actionBy, "forward")) return false;

        Map<Integer, Long> reviewersMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());

        // check that the forwardingId is present in the reviewersMap
        if (isUserAbsentFromUserList(context.getStateMachine(), reviewersMap.values(), actionBy, "reviewers",
                "forward"))
            return false;

        boolean isSerialFlow = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerialFlow) {
            // check that both, the order of forwarding, and the forwarding user
            // is present in the list of reviewers for the application in the serial approval flow.
            if (!forwardIdAndOrderCheck(context, actionBy, orderNo))
                return false;
        }

        return true;
    }

    public static boolean reject(StateContext<String, String> context) {
        MessageHeaders headers = context.getMessage().getHeaders();
        Long actionBy = get(headers, MSG_KEY_ACTION_BY, Long.class, null);
        Integer orderNo = get(headers, MSG_KEY_ORDER_NO, Integer.class, null);
        String comment = get(headers, MSG_KEY_COMMENT, String.class, null);

        // check that the reviewer requesting changes is valid
        if (isUserIdInvalid(context.getStateMachine(), actionBy, "reject")) return false;

        // check that the user rejecting the application is in the admin list
        var adminList = (List<Long>) get(context, KEY_ADMIN_IDS, List.class, Collections.emptyList());
        if (adminList.contains(actionBy)) {
            return true;
        }

        // check that the reviewer belongs to the list of reviewers for the application.
        var reviewerMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());
        if (isUserAbsentFromUserList(context.getStateMachine(), reviewerMap.values(), actionBy, "reviewers",
                "reject")) return false;

        // check that the reviwer rejecting application has input a valid comment
        if (isCommentInvalid(context.getStateMachine(), comment, "reject")) return false;

        Pair<Integer, Long> lastForwardedBy = get(context, KEY_FORWARDED_BY_LAST, Pair.class, null);

        boolean isSerialFlow = get(context, KEY_APPROVAL_FLOW_TYPE, String.class, VAL_SERIAL).equalsIgnoreCase(VAL_SERIAL);
        if (isSerialFlow) {
            // if the application has been forwarded before:
            if (lastForwardedBy != null && lastForwardedBy.getSecond() != null) {
                // check that the current user is the last person to have forwarded the application,
                // i.e., no one has forwarded the application after the current user.
                Map<Integer, Pair<Long, Boolean>> forwardMap = get(context, KEY_FORWARDED_MAP, Map.class,
                        Collections.emptyMap());
                var entrySet = new HashSet<>(forwardMap.entrySet());
                entrySet.removeIf(entry -> !entry.getValue()
                        .getSecond()); // (remove all successfully forwarded entries)
                for (var entry : entrySet) {
                    // (check if any orderNo is higher than the current user for the remaining entries.)
                    if (entry.getKey() > orderNo) {
                        String errorMsg =
                                "Guard failed for: " + "reject" + " as the reviewer: " + actionBy + " at position: " +
                                orderNo + " cannot reject the application as there are other reviewers who have already forwarded the application after them";
                        context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                        return false;
                    }
                }
            } else { // if the application has not been forwarded before:
                // check that the current user is the first order user in the reviewersMap
                if (!reviewerMap.get(1).equals(actionBy)) {
                    String errorMsg =
                            "Guard failed for reject as the Reviewer: " + actionBy + " at position: " + orderNo + " " +
                                    "cannot reject the application as they are not present in the reviewer list";
                    context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean reviewCountCheck(StateContext<String, String> context) {
        int forwardedCount = get(context, KEY_FORWARDED_COUNT, Integer.class, 0);
        int reviewerCount = get(context, KEY_REVIEWERS_COUNT, Integer.class, 0);

        // check that the application has reviewers
        if (reviewerCount <= 0) {
            context.getStateMachine().setStateMachineError(new StateMachineException("Guard failed for forward " +
                    "as there are no reviewers for the application"));
            return false;
        }

        // check that the application is not forwarded more times than total reviewers.
        if (forwardedCount + 1 > reviewerCount) {
            context.getStateMachine().setStateMachineError(new StateMachineException("Guard failed for forward" +
                    " as the application is being forwarded more times than the number of defined reviewers"));
            return false;
        }

        return true;
    }

    private static boolean forwardIdAndOrderCheck(StateContext<String, String> context, Long forwardingId, Integer forwardingOrder) {
        Map<Integer, Long> reviewersMap = (Map<Integer, Long>) get(context, KEY_REVIEWERS_MAP, Map.class, Collections.emptyMap());

        // check that the order number and the userId of the forwarding reviewer are present in the list of reviewers for the application.
        boolean isOrderNumberAndReviewerIdAbsent = reviewersMap
                .entrySet()
                .stream()
                .noneMatch(entry -> Objects.equals(entry.getKey(), forwardingOrder) &&
                        Objects.equals(entry.getValue(), forwardingId));

        if (isOrderNumberAndReviewerIdAbsent) {
            String errorMsg = "Guard Failed for: " + "forward" + " as the combination of the forwarding order " +
                    "and the forwarding userId is not present in the list of reviewers";
            context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
            return false;
        }

        Map<Integer, Pair<Long, Boolean>> forwardMap = (Map<Integer, Pair<Long, Boolean>>) get(context, KEY_FORWARDED_MAP,
                Map.class, Collections.emptyMap());

        List<Pair<Long, Boolean>> userIdAndForwardHistoryList = new ArrayList<>(forwardMap.values());

        int indexOfPairInForwardingMap = userIdAndForwardHistoryList.indexOf(new Pair<>(forwardingId, false)); // returns -1 if pair is not present.

        // check that
        // 1. the user is present in forwardingMap, and
        // 2. the same reviewer hasn't already forwarded this application before.
        boolean userIdAndForwardingHistoryAbsent = indexOfPairInForwardingMap < 0;
        if (userIdAndForwardingHistoryAbsent) {
            String errorMsg = "Guard Failed for: " +"forward" + " as no eligible reviewer found to forward the application";
            context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
            return false;
        }

        if (indexOfPairInForwardingMap == 0) {
            // if the user is the first user in the forwardMap and they have not forwarded the application before,
            // then the check is complete.
            return true;
        }

        // indexOfPairInForwardingMap > 0
        // check that all reviewers before the current one have already forwarded this application
        boolean isforwardingOrderMaintained = userIdAndForwardHistoryList.subList(0, indexOfPairInForwardingMap)
                .stream().allMatch(Pair::getSecond);
        if (!isforwardingOrderMaintained) {
            String errorMsg = "Guard Failed for: " + "forward" + " as previous reviewers have not forwarded the " +
                    "application";
            context.getStateMachine().setStateMachineError(new StateMachineException(errorMsg));
            return false;
        }

        return true;
    }


    /**
     * CHECKS
     **/


    private static boolean isUserAbsentFromUserList(StateMachine<String, String> statemachine, Collection<Long> userList,
            Long actionBy, String item, String transition) {
        if (!userList.isEmpty() && !userList.contains(actionBy)) {
            String errorMsg = "Guard failed on: " + transition + " as the user id: " + actionBy + " is not present in" +
                    " the " + item + " list: " + userList;
            statemachine.setStateMachineError(new StateMachineException(errorMsg));
            return true;
        }
        return false;
    }

    private static boolean isUserIdInvalid(StateMachine<String, String> statemachine, Long userId, String transition) {
        if (userId == null || userId == 0) {
            String errorMsg = "Guard failed on: " + transition + " as invalid userId: " + userId;
            statemachine.setStateMachineError(new StateMachineException(errorMsg));
            return true;
        }
        return false;
    }

    private static boolean isCommentInvalid(StateMachine<String, String> statemachine, String comment, String transition) {
        if (comment == null || comment.isBlank()) {
            String errorMsg = "Guard Failed for: " + transition + " as there is no valid comment provided";
            statemachine.setStateMachineError(new StateMachineException(errorMsg));
            return true;
        }
        return false;
    }

    private static boolean isCountExceedingThreshold(StateMachine<String, String> statemachine, Integer count, Integer threshold, String item, String transition) {
        if (count > threshold) {
            String errorMsg = "Guard Failed for: " + transition + " count: " + count + 1 + " for item: " + item + " " +
                    "exceeds threshold: " + threshold;
            statemachine.setStateMachineError(new StateMachineException(errorMsg));
            return true;
        }
        return false;
    }
}
