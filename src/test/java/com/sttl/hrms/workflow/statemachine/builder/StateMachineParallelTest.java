package com.sttl.hrms.workflow.statemachine.builder;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.util.EventResultHelper;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sttl.hrms.workflow.data.enums.WorkflowType.LOAN_APPLICATION;
import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_FORWARDED_COUNT;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.*;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED;

class StateMachineParallelTest {

    private final StateMachine<String, String> stateMachine;
    private static final Long applicant1 = 1L;
    private static final Long reviewer1 = 123L;
    private static final Long reviewer2 = 234L;
    private static final Long reviewer3 = 345L;

    public StateMachineParallelTest() throws Exception {
        String name = "testStateMachine";
        int reviewerCount = 3;
        Map<Integer, Long> reviewerMap = Map.of(1, reviewer1, 2, reviewer2, 3, reviewer3);
        boolean isParallel = true;
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
    void testCasesForForwardInParraller(){

        final ExtendedState extState = stateMachine.getExtendedState();
        Long wfInstanceId = 1L;

        // create and submit application for review
        List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LOAN_APPLICATION, applicant1, 0, "", E_CREATE,
                E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.passEvents(stateMachine, passEvents1);
        assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

        List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer1, 1, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents2);
        assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

        List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer2, 2, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents3);
        assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));


        List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents4);
        assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));

        assertEquals(S_CLOSED.name(), stateMachine.getState().getId());


        List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "forwarded",
                E_ROLL_BACK);
        EventSendHelper.passEvents(stateMachine, passEvents5);

        System.err.println("428 :"+extState.get(KEY_FORWARDED_COUNT, Integer.class));
        assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

        List<PassEventDto> passEvents6 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "forwarded",
                E_FORWARD);
        EventSendHelper.passEvents(stateMachine, passEvents6);
        System.err.println("428 :"+extState.get(KEY_FORWARDED_COUNT, Integer.class));
        assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));

        assertEquals(S_CLOSED.name(), stateMachine.getState().getId());

        List<PassEventDto> passEvents7 = createPassEvents(wfInstanceId, LOAN_APPLICATION, reviewer3, 3, "forwarded",
                E_TRIGGER_COMPLETE);
        EventSendHelper.passEvents(stateMachine, passEvents7);
        System.err.println("442 :"+stateMachine.isComplete());
        assertEquals(S_COMPLETED.name(), stateMachine.getState().getId());
        assertEquals(true, stateMachine.isComplete());
    }


    private static List<PassEventDto> createPassEvents(Long wfId, WorkflowType wfType, Long actionBy, Integer orderNo
            , String comment, StateMachineBuilder.SMEvent... events) {
        List<PassEventDto> passEvents = new ArrayList<>(events.length);
        for (StateMachineBuilder.SMEvent event : events) {
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
