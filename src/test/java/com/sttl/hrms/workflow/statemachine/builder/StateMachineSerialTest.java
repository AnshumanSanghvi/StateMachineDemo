package com.sttl.hrms.workflow.statemachine.builder;

import com.sttl.hrms.workflow.data.Pair;
import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.SMConstants;
import com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent;
import com.sttl.hrms.workflow.statemachine.util.EventResultHelper;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
import org.junit.jupiter.api.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sttl.hrms.workflow.data.enums.WorkflowType.LEAVE_APPLICATION;
import static com.sttl.hrms.workflow.data.enums.WorkflowType.LOAN_APPLICATION;
import static com.sttl.hrms.workflow.statemachine.SMConstants.*;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.*;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED;

class StateMachineSerialTest {

    private final StateMachine<String, String> stateMachine;

    public StateMachineSerialTest() throws Exception {
        String name = "testStateMachine";
        int reviewerCount = 3;
        Map<Integer, Long> reviewerMap = Map.of(1, 123L, 2, 234L, 3, 345L);
        boolean isParallel = false;
        int maxChangeReq = 3;
        int maxRollBack = 3;
        this.stateMachine = StateMachineBuilder.createStateMachine(name, reviewerCount, reviewerMap, isParallel,
                maxChangeReq, maxRollBack);
    }

    @BeforeEach
    void setUp() {
        stateMachine.startReactively().block();
    }

    @AfterEach
    void tearDown() {
        stateMachine.stopReactively().block();
    }

    @RepeatedTest(3)
    void testStateMachineResets() {
        Message<String> message = MessageBuilder.withPayload(E_CREATE.name()).build();
        var resultFlux = stateMachine.sendEvent(Mono.just(message));
        var resultList = EventResultHelper.processResultFlux(resultFlux);
        assertTrue(resultList.stream().allMatch(res -> res.getResultType().equals(ACCEPTED)));
    }

    @Test
    void happyPath() {
        final ExtendedState extState = stateMachine.getExtendedState();

        // create and submit an application for review
        List<PassEventDto> passEvents1 = createPassEvents(0L, LEAVE_APPLICATION, 1L, 0, "", E_CREATE,
                E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.passEvents(stateMachine, passEvents1);
        assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

        // first reviewer forwards the application
        List<PassEventDto> passEvents2 = createPassEvents(0L, LEAVE_APPLICATION, 123L, 1, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents2);
        assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
        assertEquals(new Pair<>(1, 123L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

        // second reviewer forwards the application
        List<PassEventDto> passEvents3 = createPassEvents(0L, LEAVE_APPLICATION, 234L, 2, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents3);
        assertEquals(2, extState.get(SMConstants.KEY_FORWARDED_COUNT, Integer.class));
        assertEquals(new Pair<>(2, 234L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

        // third reviewer forwards (approves) the application
        List<PassEventDto> passEvents4 = createPassEvents(0L, LEAVE_APPLICATION, 345L, 3, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents4);
        assertEquals(3, extState.get(SMConstants.KEY_FORWARDED_COUNT, Integer.class));
        assertEquals(new Pair<>(3, 345L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

        // application approved and completed.
        assertEquals(S_COMPLETED.name(), stateMachine.getState().getId());
        assertTrue(stateMachine.isComplete());
    }


    @Nested
    public class RequestChangesTest {
        @Test
        void requestChangesHappyPath() {
            final ExtendedState extState = stateMachine.getExtendedState();

            // create and submit application for review
            List<PassEventDto> passEvents1 = createPassEvents(1L, LOAN_APPLICATION, 1L, 0, "", E_CREATE,
                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
            EventSendHelper.passEvents(stateMachine, passEvents1);
            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

            // reviewer 1 requests changes
            List<PassEventDto> passEvents2 = createPassEvents(1L, LOAN_APPLICATION, 123L, 1, "please make changes",
                    E_REQUEST_CHANGES_IN);
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));

            // user submits application for review again
            passEvents1.removeIf(passEvent -> passEvent.getEvent().equalsIgnoreCase(E_CREATE.name()));
            EventSendHelper.passEvents(stateMachine, passEvents1);

            // first reviewer forwards the application
            List<PassEventDto> passEvents3 = createPassEvents(1L, LOAN_APPLICATION, 123L, 1, "forwarded",
                    E_FORWARD);
            EventSendHelper.passEvents(stateMachine, passEvents3);
            assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(new Pair<>(1, 123L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

            // second reviewer forwards the application
            List<PassEventDto> passEvents4 = createPassEvents(1L, LOAN_APPLICATION, 234L, 2, "forwarded",
                    E_FORWARD);
            EventSendHelper.passEvents(stateMachine, passEvents4);
            assertEquals(2, extState.get(SMConstants.KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(new Pair<>(2, 234L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

            // third reviewer requests changes
            List<PassEventDto> passEvents5 = createPassEvents(1L, LOAN_APPLICATION, 345L, 3, "please make changes",
                    E_REQUEST_CHANGES_IN);
            EventSendHelper.passEvents(stateMachine, passEvents5);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(2, extState.get(KEY_RETURN_COUNT, Integer.class));

        }

        @Test
        void requestChangesLimit() {
            final ExtendedState extState = stateMachine.getExtendedState();

            // create and submit application for review
            List<PassEventDto> passEvents1 = createPassEvents(1L, LOAN_APPLICATION, 1L, 0, "", E_CREATE,
                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
            EventSendHelper.passEvents(stateMachine, passEvents1);
            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

            // reviewer 1 requests changes
            List<PassEventDto> passEvents2 = createPassEvents(1L, LOAN_APPLICATION, 123L, 1, "please make changes",
                    E_REQUEST_CHANGES_IN);
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));

            // user submits application for review again
            passEvents1.removeIf(passEvent -> passEvent.getEvent().equalsIgnoreCase(E_CREATE.name()));
            EventSendHelper.passEvents(stateMachine, passEvents1);

            // reviewer 1 requests changes 2nd time
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(2, extState.get(KEY_RETURN_COUNT, Integer.class));

            // user submits application for review again
            EventSendHelper.passEvents(stateMachine, passEvents1);

            // reviewer 1 requests changes 3rd time
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(3, extState.get(KEY_RETURN_COUNT, Integer.class));

            // user submits application for review again
            EventSendHelper.passEvents(stateMachine, passEvents1);

            // reviewer 1 and reviewer 2 forwards the application
            List<PassEventDto> passEvents3 = createPassEvents(1L, LEAVE_APPLICATION, 123L, 1, "forwarded",
                    E_FORWARD);
            EventSendHelper.passEvents(stateMachine, passEvents3);
            List<PassEventDto> passEvents4 = createPassEvents(1L, LOAN_APPLICATION, 234L, 2, "forwarded",
                    E_FORWARD);
            EventSendHelper.passEvents(stateMachine, passEvents4);

            // reviewer 3 requests changes (total, 4th time)
            List<PassEventDto> passEvents5 = createPassEvents(1L, LOAN_APPLICATION, 345L, 3, "please make changes",
                    E_REQUEST_CHANGES_IN);
            EventSendHelper.passEvents(stateMachine, passEvents5);
            assertEquals(3, extState.get(KEY_RETURN_COUNT, Integer.class));
            assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
        }

        @Test
        void requestChangesOrder() {
            final ExtendedState extState = stateMachine.getExtendedState();

            // create and submit application for review
            List<PassEventDto> passEvents1 = createPassEvents(1L, LOAN_APPLICATION, 1L, 0, "", E_CREATE,
                    E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
            EventSendHelper.passEvents(stateMachine, passEvents1);
            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

            // test that if reviewer 2 requests changes then they won't be accepted as its out of reviewer order
            List<PassEventDto> passEvents2 = createPassEvents(1L, LOAN_APPLICATION, 234L, 2, "please make changes",
                    E_REQUEST_CHANGES_IN);
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());
            assertEquals(0, extState.get(KEY_RETURN_COUNT, Integer.class));

            // first reviewer forwards the application
            List<PassEventDto> passEvents3 = createPassEvents(1L, LOAN_APPLICATION, 123L, 1, "forwarded",
                    E_FORWARD);
            EventSendHelper.passEvents(stateMachine, passEvents3);
            assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(new Pair<>(1, 123L), extState.get(KEY_LAST_FORWARDED_BY, Pair.class));

            // test that now if reviewer 2 requests changes then they will be accepted
            EventSendHelper.passEvents(stateMachine, passEvents2);
            assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
            assertEquals(S_CREATED.name(), stateMachine.getState().getId());
            assertEquals(1, extState.get(KEY_RETURN_COUNT, Integer.class));
        }
    }


    private static List<PassEventDto> createPassEvents(Long wfId, WorkflowType wfType, Long actionBy, Integer orderNo
            , String comment, SMEvent... events) {
        List<PassEventDto> passEvents = new ArrayList<>(events.length);
        for (SMEvent event : events) {
            passEvents.add(PassEventDto.builder()
                    .actionBy(actionBy)
                    .actionDate(LocalDateTime.now())
                    .comment(comment)
                    .event(event.name())
                    .orderNo(orderNo)
                    .workflowInstanceId(wfId)
                    .workflowType(wfType)
                    .build());
        }

        return passEvents;
    }
}