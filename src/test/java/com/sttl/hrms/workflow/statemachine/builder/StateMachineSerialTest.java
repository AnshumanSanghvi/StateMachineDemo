//package com.sttl.hrms.workflow.statemachine.builder;
//
//import static com.sttl.hrms.workflow.data.enums.WorkflowType.LEAVE_APPLICATION;
//import static com.sttl.hrms.workflow.data.enums.WorkflowType.LOAN_APPLICATION;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_CLOSED_BY;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_CLOSED_STATE_TYPE;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_FORWARDED_BY_LAST;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_FORWARDED_COUNT;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_REJECTED_BY;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_RETURN_COUNT;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_ROLL_BACK_BY_LAST;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_ROLL_BACK_COUNT;
//import static com.sttl.hrms.workflow.statemachine.SMConstants.VAL_CANCELED;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_CANCEL;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_CREATE;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_FORWARD;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_REJECT;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_REQUEST_CHANGES_IN;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_ROLL_BACK;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_SUBMIT;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_FLOW_JUNCTION;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_REVIEW_OF;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_CLOSED;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_COMPLETED;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_CREATED;
//import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_SERIAL_APPROVAL_FLOW;
//import static com.sttl.hrms.workflow.statemachine.util.ExtStateUtil.get;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.statemachine.ExtendedState;
//import org.springframework.statemachine.StateMachine;
//
//import com.sttl.hrms.workflow.data.Pair;
//import com.sttl.hrms.workflow.data.enums.WorkflowType;
//import com.sttl.hrms.workflow.resource.dto.PassEventDto;
//import com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent;
//import com.sttl.hrms.workflow.statemachine.util.EventResultHelper;
//import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
//
//import reactor.core.publisher.Mono;
//
//class StateMachineSerialTest {
//
//    private final StateMachine<String, String> stateMachine;
//    private static final Long applicant1 = 1L;
//    private static final List<Long> reviewer1 = new ArrayList<>(Arrays. asList(123L, 122L));
//    private static final List<Long> reviewer2 = new ArrayList<>(Arrays. asList(234L, 233L));;
//    private static final List<Long> reviewer3 =new ArrayList<>(Arrays. asList(345L, 344L)); ;
//
//    public StateMachineSerialTest() throws Exception {
//        String name = "testStateMachine";
//        int reviewerCount = 3;
//        Map<Integer, List<Long>> reviewerMap = Map.of(1, reviewer1, 2, reviewer2, 3, reviewer3);
//        boolean isParallel = false;
//        int maxChangeReq = 3;
//        int maxRollBack = 3;
//        this.stateMachine = StateMachineBuilder.createStateMachine(name, reviewerCount, reviewerMap, isParallel,
//                maxChangeReq, maxRollBack);
//    }
//
//    @BeforeEach
//    void setUp() {
//        stateMachine.startReactively().block();
//    }
//
//    @AfterEach
//    void tearDown() {
//        stateMachine.stopReactively().block();
//    }
//
//    @RepeatedTest(3)
//    void testStateMachineResets() {
//        Message<String> message = MessageBuilder.withPayload(E_CREATE.name()).build();
//        var resultFlux = stateMachine.sendEvent(Mono.just(message));
//        var resultList = EventResultHelper.processResultFlux(resultFlux);
//        assertTrue(resultList.stream().allMatch(res -> res.getResultType().equals(ACCEPTED)));
//    }
//
//    @Test
//    void testForward() {
//        final ExtendedState extState = stateMachine.getExtendedState();
//        Long wfInstanceId = 0L;
//
//        // create and submit an application for review
//        List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, applicant1, 0, "", E_CREATE,
//                E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//        EventSendHelper.passEvents(stateMachine, passEvents1);
//        assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//
//        // first reviewer forwards the application
//        List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, reviewer1, 1, "forwarded",
//                E_FORWARD);
//        EventSendHelper.passEvents(stateMachine, passEvents2);
//        assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//        assertEquals(new Pair<>(1, reviewer1), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//        // second reviewer forwards the application
//        List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, reviewer2, 2, "forwarded",
//                E_FORWARD);
//        EventSendHelper.passEvents(stateMachine, passEvents3);
//        assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//        assertEquals(new Pair<>(2, reviewer2), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//        // third reviewer forwards (approves) the application
//        List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, reviewer3, 3, "forwarded",
//                E_FORWARD);
//        EventSendHelper.passEvents(stateMachine, passEvents4);
//        assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//        assertEquals(new Pair<>(3, reviewer3), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//        // application approved and completed.
//        assertEquals(S_CLOSED.name(), stateMachine.getState().getId());
//    }
//
//    @Test
//    void testReject() {
//        Long wfInstanceId = 1L;
//        ExtendedState extState = stateMachine.getExtendedState();
//        List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 0, "approved",
//                E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION, E_REJECT);
//        EventSendHelper.passEvents(stateMachine, passEvents2);
//        assertEquals(S_CLOSED.name(), stateMachine.getState().getId());
//        assertEquals(reviewer1, get(extState, KEY_REJECTED_BY, Long.class, null));
//    }
//
//    @Test
//    void testCancel() {
//        final ExtendedState extState = stateMachine.getExtendedState();
//        Long wfInstanceId = 1L;
//
//        List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 0, "approved",
//                E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//        EventSendHelper.passEvents(stateMachine, passEvents4);
//
//        // CANCEL TEST CASE IT'S APPEND ABOVE
//        List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "canceled",
//                E_CANCEL);
//        EventSendHelper.passEvents(stateMachine, passEvents5);
//        assertEquals(reviewer1, get(extState, KEY_CLOSED_BY, Long.class, null));
//        assertEquals(VAL_CANCELED, get(extState, KEY_CLOSED_STATE_TYPE, String.class, ""));
//        assertEquals(S_COMPLETED.name(), stateMachine.getState().getId());
//    }
//
//    @Nested
//    public class RequestChangesTest {
//        @Test
//        void requestChangesHappyPath() {
//            final ExtendedState extState = stateMachine.getExtendedState();
//            Long wfInstanceId = 1L;
//
//            // create and submit application for review
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "", E_CREATE,
//                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//
//            // reviewer 1 requests changes
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "please make " +
//                            "changes", E_REQUEST_CHANGES_IN);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//            // user submits application for review again
//            passEvents1.removeIf(passEvent -> passEvent.getEvent().equalsIgnoreCase(E_CREATE.name()));
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            // first reviewer forwards the application
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forwarded",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//            assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(new Pair<>(1, reviewer1), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//            // second reviewer forwards the application
//            List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forwarded",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents4);
//            assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(new Pair<>(2, reviewer2), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//            // third reviewer requests changes
//            List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "please make " +
//                            "changes",
//                    E_REQUEST_CHANGES_IN);
//            EventSendHelper.passEvents(stateMachine, passEvents5);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(2, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//        }
//
//        @Test
//        void requestChangesLimit() {
//            final ExtendedState extState = stateMachine.getExtendedState();
//            Long wfInstanceId = 1L;
//
//            // create and submit application for review
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "", E_CREATE,
//                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//
//            // reviewer 1 requests changes
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "please make changes",
//                    E_REQUEST_CHANGES_IN);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//            // user submits application for review again
//            passEvents1.removeIf(passEvent -> passEvent.getEvent().equalsIgnoreCase(E_CREATE.name()));
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            // reviewer 1 requests changes 2nd time
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(2, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//            // user submits application for review again
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            // reviewer 1 requests changes 3rd time
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(3, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//            // user submits application for review again
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            // reviewer 1 and reviewer 2 forwards the application
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, reviewer1, 1, "forwarded",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//            List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forwarded",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents4);
//
//            // reviewer 3 requests changes (total, 4th time)
//            List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "please make " +
//                            "changes",
//                    E_REQUEST_CHANGES_IN);
//            EventSendHelper.passEvents(stateMachine, passEvents5);
//            assertEquals(3, extState.get(KEY_RETURN_COUNT, Integer.class));
//            assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//        }
//
//        @Test
//        void requestChangesOrder() {
//            final ExtendedState extState = stateMachine.getExtendedState();
//            Long wfInstanceId = 1L;
//
//            // create and submit application for review
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "", E_CREATE,
//                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//
//            // test that if reviewer 2 requests changes then they won't be accepted as its out of reviewer order
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "please make " +
//                            "changes", E_REQUEST_CHANGES_IN);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//            assertEquals(0, extState.get(KEY_RETURN_COUNT, Integer.class));
//
//            // first reviewer forwards the application
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forwarded",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//            assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(new Pair<>(1, reviewer1), extState.get(KEY_FORWARDED_BY_LAST, Pair.class));
//
//            // test that now if reviewer 2 requests changes then they will be accepted
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
//            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));
//        }
//    }
//
//    @Nested
//    public class RollbackTest {
//
//        @SuppressWarnings("unchecked")
//        @Test
//        void testRollbackFromReject() {
//
//            final ExtendedState extState = stateMachine.getExtendedState();
//            Long wfInstanceId = 1L;
//
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "create " +
//                            "application", E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 0, "rejected",
//                    E_REJECT);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 0, "rolling " +
//                    "back rejection", E_ROLL_BACK);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//
//            assertEquals(1, get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
//            Long actualRollBackBy = Optional.ofNullable(get(extState, KEY_ROLL_BACK_BY_LAST, Pair.class, null))
//                    .map(pair -> (Pair<Integer, Long>) pair)
//                    .map(Pair::getSecond)
//                    .orElse(0L);
//            assertEquals(reviewer1, actualRollBackBy);
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//        }
//
//        @Test
//        void testRollbackFromForward() {
//            Long wfInstanceId = 1L;
//
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "create " +
//                            "application", E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//
//            List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "roll back",
//                    E_ROLL_BACK);
//            EventSendHelper.passEvents(stateMachine, passEvents4);
//
//            final ExtendedState extState = stateMachine.getExtendedState();
//            assertEquals(new Pair<>(1, reviewer1), get(extState, KEY_FORWARDED_BY_LAST, Pair.class, null));
//            assertEquals(new Pair<>(2, reviewer2), (get(extState, KEY_ROLL_BACK_BY_LAST, Pair.class, null)));
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//            assertEquals(1, get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
//        }
//
//        @Test
//        void testRollbackFromApprove() {
//            Long wfInstanceId = 1L;
//
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "create " +
//                            "application", E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//
//            List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents4);
//
//            List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "roll back",
//                    E_ROLL_BACK);
//            EventSendHelper.passEvents(stateMachine, passEvents5);
//
//            final ExtendedState extState = stateMachine.getExtendedState();
//            assertEquals(new Pair<>(2, reviewer2), get(extState, KEY_FORWARDED_BY_LAST, Pair.class, null));
//            assertEquals(new Pair<>(3, reviewer3), get(extState, KEY_ROLL_BACK_BY_LAST, Pair.class, null));
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//            assertEquals(1, get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
//        }
//
//        @Test
//        void testRollbackOrder() {
//            Long wfInstanceId = 1L;
//
//            List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "create " +
//                    "application", E_CREATE, E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
//            EventSendHelper.passEvents(stateMachine, passEvents1);
//
//            List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents2);
//
//            List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forward",
//                    E_FORWARD);
//            EventSendHelper.passEvents(stateMachine, passEvents3);
//
//            List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "roll back",
//                    E_ROLL_BACK);
//            EventSendHelper.passEvents(stateMachine, passEvents4);
//
//            final ExtendedState extState = stateMachine.getExtendedState();
//            assertEquals(new Pair<>(2, reviewer2), get(extState, KEY_FORWARDED_BY_LAST, Pair.class, null));
//            assertEquals(new Pair<>(null, null), get(extState, KEY_ROLL_BACK_BY_LAST, Pair.class, null));
//            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
//            assertEquals(0, get(extState, KEY_ROLL_BACK_COUNT, Integer.class, 0));
//        }
//
//
//    }
//
//    private static List<PassEventDto> createPassEvents(Long wfId, WorkflowType wfType, Long actionBy, Integer orderNo
//            , String comment, SMEvent... events) {
//        List<PassEventDto> passEvents = new ArrayList<>(events.length);
//        for (SMEvent event : events) {
//            passEvents.add(PassEventDto.builder()
//                    .actionBy(actionBy)
//                    .actionDate(LocalDateTime.now())
//                    .comment(comment)
//                    .event(event.name())
//                    .orderNo(orderNo)
//                    .workflowInstanceId(wfId)
//                    .workflowType(wfType)
//                    .build());
//        }
//
//        return passEvents;
//    }
//}