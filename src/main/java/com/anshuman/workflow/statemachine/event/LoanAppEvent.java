package com.anshuman.workflow.statemachine.event;

import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.statemachine.data.Pair;
import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.state.LoanAppState;
import com.anshuman.workflow.statemachine.util.EventResultHelper;
import com.anshuman.workflow.statemachine.util.EventSendHelper;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
public enum LoanAppEvent {
    E_INITIALIZE("Initialize Loan Application"),
    E_SUBMIT("Submit Loan Application"),
    E_TRIGGER_REVIEW_OF("Request Review of Loan Application"),
    E_REQUEST_CHANGES_IN("Request Changes to Submitted Loan Application"),
    E_TRIGGER_FLOW_JUNCTION("Transition to the approval flow type junction"),
    E_FORWARD("Forward Loan Application to the next Approver"),
    E_APPROVE("Approve Loan Application"),
    E_REJECT("Reject Loan Application"),
    E_CANCEL("Cancel Loan Application"),
    E_ROLL_BACK("Roll Back Decision on Loan Application"),
    E_TRIGGER_COMPLETE("Close Loan Application");

    @Getter
    private final String humanReadableStatus;
    
    private static final LoanAppEvent[] values = LoanAppEvent.values();

    public static LoanAppEvent getByName(String name) {
        for (LoanAppEvent e : values) {
            if (e.name().equalsIgnoreCase(name))
                return e;
        }
        throw new IllegalArgumentException("No event with the given name found");
    }

    public static Pair<StateMachine<LoanAppState, LoanAppEvent>, List<EventResultDTO<LoanAppState, LoanAppEvent>>> passEvent(StateMachine<LoanAppState,
        LoanAppEvent> stateMachine,
        PassEventDto eventDto) {

        LoanAppEvent event = LoanAppEvent.getByName(eventDto.getEvent());
        Long actionBy = eventDto.getActionBy();
        Integer order = eventDto.getOrderNo();
        String comment = eventDto.getComment();

        // send the event to the state machine
        Flux<StateMachineEventResult<LoanAppState, LoanAppEvent>> resultFlux = switch (event) {
            case E_INITIALIZE, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION,
                E_REJECT, E_CANCEL, E_TRIGGER_COMPLETE -> EventSendHelper.sendEvent(stateMachine, event);
            case E_SUBMIT -> EventSendHelper.sendEvents(stateMachine, event, E_TRIGGER_REVIEW_OF);
            case E_APPROVE -> EventSendHelper.sendEvents(stateMachine, event, E_TRIGGER_COMPLETE);
            case E_REQUEST_CHANGES_IN -> EventSendHelper.sendRequestChangesEvent(stateMachine, event, order, actionBy, comment);
            case E_FORWARD -> EventSendHelper.sendForwardEvent(stateMachine, event, order, actionBy, comment);
            case E_ROLL_BACK -> EventSendHelper.sendRollBackApprovalEvent(stateMachine, event, order, actionBy);
        };

        // parse the result
        List<EventResultDTO<LoanAppState, LoanAppEvent>> resultDTOList = EventResultHelper.processResultFlux(resultFlux);
        log.debug("After passing event: {}, resultFlux is: {}", event, resultDTOList);
        return new Pair<>(stateMachine, resultDTOList);
    }
}
