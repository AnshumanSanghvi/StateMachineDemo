package com.sttl.hrms.workflow.statemachine.event;

import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.data.Pair;
import com.sttl.hrms.workflow.statemachine.data.dto.EventResultDTO;
import com.sttl.hrms.workflow.statemachine.state.LeaveAppState;
import com.sttl.hrms.workflow.statemachine.util.EventResultHelper;
import com.sttl.hrms.workflow.statemachine.util.EventSendHelper;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public enum LeaveAppEvent {
    E_INITIALIZE("Initialize Leave Application"),
    E_SUBMIT("Submit Leave Application"),
    E_TRIGGER_REVIEW_OF("Request Review of Leave Application"),
    E_REQUEST_CHANGES_IN("Request Changes to Submitted Leave Application"),
    E_TRIGGER_FLOW_JUNCTION("Transition to the approval flow type junction"),
    E_FORWARD("Forward Leave Application to the next Approver"),
    E_APPROVE("Approve Leave Application"),
    E_REJECT("Reject Leave Application"),
    E_CANCEL("Cancel Leave Application"),
    E_ROLL_BACK("Roll Back Decision on Leave Application"),
    E_TRIGGER_COMPLETE("Close Leave Application");

    @Getter
    private final String humanReadableStatus;

    private static final LeaveAppEvent[] values = LeaveAppEvent.values();

    LeaveAppEvent(String humanReadableStatus) {
        this.humanReadableStatus = humanReadableStatus;
    }

    public static LeaveAppEvent getByName(String name) {
        for (LeaveAppEvent e : values) {
            if (e.name().equalsIgnoreCase(name))
                return e;
        }
        throw new IllegalArgumentException("No event with the given name found");
    }

    public static Pair<StateMachine<LeaveAppState, LeaveAppEvent>, List<EventResultDTO<LeaveAppState, LeaveAppEvent>>> passEvent(StateMachine<LeaveAppState,
        LeaveAppEvent> stateMachine,
        PassEventDto eventDto) {

        LeaveAppEvent event = LeaveAppEvent.getByName(eventDto.getEvent());
        Long actionBy = eventDto.getActionBy();
        Integer order = eventDto.getOrderNo();
        String comment = eventDto.getComment();

        // send the event to the state machine
        var resultFlux = switch (event) {
            case E_INITIALIZE, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION,
                E_REJECT, E_CANCEL, E_TRIGGER_COMPLETE -> EventSendHelper.sendEvent(stateMachine, event);
            case E_SUBMIT -> EventSendHelper.sendEvents(stateMachine, event, E_TRIGGER_REVIEW_OF);
            case E_APPROVE -> EventSendHelper.sendEvents(stateMachine, event, E_TRIGGER_COMPLETE);
            case E_REQUEST_CHANGES_IN -> EventSendHelper.sendRequestChangesEvent(stateMachine, event, order, actionBy, comment);
            case E_FORWARD -> EventSendHelper.sendForwardEvent(stateMachine, event, order, actionBy, comment);
            case E_ROLL_BACK -> EventSendHelper.sendRollBackApprovalEvent(stateMachine, event, order, actionBy);
        };

        // parse the result
        List<EventResultDTO<LeaveAppState, LeaveAppEvent>> resultDTOList = EventResultHelper.processResultFlux(resultFlux);
        log.debug("After passing event: {}, resultFlux is: {}", event, resultDTOList);
        return new Pair<>(stateMachine, resultDTOList);
    }

}
