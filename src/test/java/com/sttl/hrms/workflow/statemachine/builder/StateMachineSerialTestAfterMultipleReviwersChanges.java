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
import static com.sttl.hrms.workflow.statemachine.util.ExtStateUtil.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED;
import static com.sttl.hrms.workflow.data.enums.WorkflowType.LEAVE_APPLICATION;

import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMState.S_SERIAL_APPROVAL_FLOW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

class StateMachineSerialTestAfterMultipleReviwersChanges {

	private final StateMachine<String, String> stateMachine;
	private static final List<Long> applicant1 = new ArrayList<>(Arrays.asList(1L));
	private static final List<Long> reviewer1 = new ArrayList<>(Arrays.asList(123L, 122L));
	private static final List<Long> reviewer2 = new ArrayList<>(Arrays.asList(234L, 233L));;
	private static final List<Long> reviewer3 = new ArrayList<>(Arrays.asList(345L, 344L));;

	public StateMachineSerialTestAfterMultipleReviwersChanges() throws Exception {
		String name = "testStateMachine";
		int reviewerCount = 3;
		Map<Integer, List<Long>> reviewerMap = Map.of(1, reviewer1, 2, reviewer2, 3, reviewer3);
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
	void testCasesForSerialFlow() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

	}

	@Test
	void testCasesForSerialFlowCancel() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// CANCEL

		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0,
				"CANCEL HO GYA RE BABA", E_CANCEL);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(S_COMPLETED.name(), stateMachine.getState().getId());
		

	}

	@Test
	void testCasesForSerialFlowReject() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// E_REJECT

		List<PassEventDto> passEvents12 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2,
				"REJECT HO GYA BHAI", E_REJECT);
		EventSendHelper.passEvents(stateMachine, passEvents12);
		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());
		

	}

	@Test
	void testCasesForSerialFlowCForwardAndApprovalAfter3Approval() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// FORWARD AND APPROVE AFTER 3 APPROVAL APPROVE

		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded to 2 ",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

//
//		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded to 3",
//				E_FORWARD);
//		EventSendHelper.passEvents(stateMachine, passEvents3);
//		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//
//
//		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "Approve",
//				E_FORWARD);
//		EventSendHelper.passEvents(stateMachine, passEvents4);
//		assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));
//		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());
//


	}

	@Test
	void testCasesForSerialFlowRollbackwhileApplicationIsFlow() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// ROLLBACK WHILE APPLICATION IS IN S_SERIAL_APPROVAL_FLOW

		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded to 2 ",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		
		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded to 3",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "rollback to 2",
				E_ROLL_BACK);
		EventSendHelper.passEvents(stateMachine, passEvents4);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

	}

	@Test
	void testCasesForSerialFlowRollbackWhileApplicationIsClosed() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// ROLLBACK WHILE APPLICATION IS CLOSED

		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded to 2 ",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded to 3",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "Approve",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents4);
		assertEquals(3, extState.get(KEY_FORWARDED_COUNT, Integer.class));
		assertEquals(S_CLOSED.name(), stateMachine.getState().getId());

		

		List<PassEventDto> passEvents5 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "rollback",
				E_ROLL_BACK);
		EventSendHelper.passEvents(stateMachine, passEvents5);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		

	}

	@Test
	void testCasesForSerialFlowRequestChanges() {

		final ExtendedState extState = stateMachine.getExtendedState();
		Long wfInstanceId = 1L;

		// create and submit application for review
		List<PassEventDto> passEvents1 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 50L, 0, "", E_CREATE,
				E_SUBMIT, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
		EventSendHelper.passEvents(stateMachine, passEvents1);
		assertEquals(S_SERIAL_APPROVAL_FLOW.name(), stateMachine.getState().getId());

		// E_REQUEST_CHANGES_IN

		List<PassEventDto> passEvents2 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 122L, 1, "forwarded to 2 ",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents2);
		assertEquals(1, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents3 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 234L, 2, "forwarded to 3",
				E_FORWARD);
		EventSendHelper.passEvents(stateMachine, passEvents3);
		assertEquals(2, extState.get(KEY_FORWARDED_COUNT, Integer.class));

		

		List<PassEventDto> passEvents4 = createPassEvents(wfInstanceId, LEAVE_APPLICATION, 344L, 3, "forwarded to 3",
				E_REQUEST_CHANGES_IN);
		EventSendHelper.passEvents(stateMachine, passEvents4);
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
