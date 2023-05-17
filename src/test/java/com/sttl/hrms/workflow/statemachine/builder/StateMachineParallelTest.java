package com.sttl.hrms.workflow.statemachine.builder;

import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_FORWARDED_COUNT;
import static com.sttl.hrms.workflow.statemachine.SMConstants.KEY_FORWARDED_MAP;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_CREATE;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_FORWARD;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_ROLL_BACK;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_SUBMIT;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_COMPLETE;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_FLOW_JUNCTION;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_TRIGGER_REVIEW_OF;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_REQUEST_CHANGES_IN;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_CANCEL;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.E_REJECT;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_CLOSED;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_COMPLETED;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_PARALLEL_APPROVAL_FLOW;
import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_CREATED;
import static com.sttl.hrms.workflow.statemachine.util.ExtStateUtil.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED;
import static com.sttl.hrms.workflow.data.enums.WorkflowType.LEAVE_APPLICATION;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;

import com.sttl.hrms.workflow.data.enums.WorkflowType;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.util.EventResultHelper;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;

import reactor.core.publisher.Mono;

class StateMachineParallelTest {

    private final StateMachine<String, String> stateMachine;
    private static final List<Long> applicant1 = new ArrayList<>(Arrays. asList(1L));
    private static final List<Long> reviewer1 = new ArrayList<>(Arrays. asList(123L, 122L));
    private static final List<Long> reviewer2 = new ArrayList<>(Arrays. asList(234L, 233L));;
    private static final List<Long> reviewer3 =new ArrayList<>(Arrays. asList(345L, 344L)); ;

    public StateMachineParallelTest() throws Exception {
        String name = "testStateMachine";
        int reviewerCount = 3;
        Map<Integer, List<Long>> reviewerMap = Map.of(1, reviewer1, 2, reviewer2, 3, reviewer3);
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
    void testCasesInParraller(){

        final ExtendedState extState = stateMachine.getExtendedState();
        Long wfInstanceId = 1L;

        // create and submit application for review
        List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
                E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
        EventSendHelper.passEvents(stateMachine, passEvents1);
        assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

      
    }
    
    
	@Test
	void testCasesInParrallerCancel() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// E_CANCEL

		List<PassEventDto> passEvents11 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "Cancel By",
				E_CANCEL);
		EventSendHelper.passEvents(stateMachine, passEvents11);
		assertEquals(S_COMPLETED.name(), stateMachine.getState().getId());

		

	}

	@Test
	void testCasesInParrallerReject() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// E_REJECT

		List<PassEventDto> passEvents12 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "reject",
				E_REJECT);
		EventSendHelper.passEvents(stateMachine, passEvents12);
		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());

		
	}

	@Test
	void testCasesInParraller3ForwardApprovalAfterApplicationApprovedAndClosed() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		
		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded to 2",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded to 3",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "approved",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents4);
		assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());

	}

	@Test
	void testCasesForForwardInParrallerRollbackAfter3LevelApprovedClosedToInS_Parrallel_Flow() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		
		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "forwarded",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents4);
		assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());

		List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "forwarded",
				E_ROLL_BACK);
		EventSendHelper.passEvents(stateMachine, passEvents5);

		
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

	}

	@Test
	void testCasesInParrallerRequestChanges() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_PARALLEL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		
		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		// E_REQUEST_CHANGES_IN

		List<PassEventDto> passEvents10 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 3L, 3, "request changes",
				E_REQUEST_CHANGES_IN);
		EventSendHelper.passEvents(stateMachine, passEvents10);

		assertEquals(S_CREATED.name(),  stateMachine.getState().getId());
		assertEquals(0, extState.get(KEY_FORWARDED_COUNT, Integer.class));
		
		

	}

	private static List<PassEventDto> createPassEvents(Long wfId, WorkflowType wfType, Long actionBy, Integer orderNo,
			String comment, StateMachineBuilder.SMEvent... events) {
		List<PassEventDto> passEvents = new ArrayList<>(events.length);
		for (StateMachineBuilder.SMEvent event : events) {
			passEvents.add(PassEventDto.builder().actionBy(actionBy).actionDate(LocalDateTime.now()).comment(comment)
					.event(event.name()).orderNo(orderNo).workflowInstanceId(wfId).workflowType(wfType).build());
		}

		return passEvents;
	}
}
