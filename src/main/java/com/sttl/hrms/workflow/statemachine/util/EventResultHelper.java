package com.sttl.hrms.workflow.statemachine.util;

import com.sttl.hrms.workflow.statemachine.EventResultDto;
import com.sttl.hrms.workflow.statemachine.exception.StateMachineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineEventResult;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class EventResultHelper {

    private EventResultHelper() {
        // use class statically
    }

    public static Flux<EventResultDto> toResultDTOFlux(Flux<StateMachineEventResult<String, String>> resultFlux) {
        return resultFlux
                .map(EventResultDto::new)
                .doOnError(ex -> {
                    throw new StateMachineException("Error parsing the results in the state machine.", ex);
                })
                .onErrorStop();
    }

    public static List<EventResultDto> toResultDTOList(Flux<StateMachineEventResult<String, String>> resultFlux) {
        List<EventResultDto> eventResults = EventResultHelper
                .toResultDTOFlux(resultFlux)
                .collectList()
                .block();
        log.trace("Parsing StateMachine event results to list: {}", eventResults);
        return eventResults;
    }

    public static String toResultDTOString(Flux<StateMachineEventResult<String, String>> resultFlux) {
        return "[" + Optional
                .ofNullable(toResultDTOList(resultFlux))
                .or(() -> Optional.of(Collections.emptyList()))
                .stream()
                .flatMap(Collection::stream)
                .map(EventResultDto::toString)
                .collect(Collectors.joining(",\n")) + "]";
    }

    public static List<EventResultDto> processResultFlux(Flux<StateMachineEventResult<String, String>> resultFlux) {
        // parse the result
        List<EventResultDto> resultDTOList = EventResultHelper.toResultDTOList(resultFlux);
        log.debug("resultFlux is: {}", resultDTOList);

        // empty result
        if (resultDTOList == null || resultDTOList.isEmpty()) {
            log.warn("state machine event result was empty");
            return Collections.emptyList();
        }

        // find out if any event wasn't accepted.
        boolean hasErrors = resultDTOList.stream().anyMatch(Predicate.not(EventResultDto.accepted));

        // throw error if the event is not accepted by the state machine.
        if (hasErrors) {
            String eventStr = resultDTOList
                    .stream()
                    .filter(Predicate.not(EventResultDto.accepted))
                    .map(EventResultDto::getEvent)
                    .map(StringUtil::event)
                    .collect(Collectors.joining(", "));

            log.error("Did not persist the state machine context to the database, "
                    + "as the following passed event: [" + eventStr + "]" +
                    " were not accepted by the statemachine ");

            return Collections.emptyList();
        }

        return resultDTOList;
    }

}
