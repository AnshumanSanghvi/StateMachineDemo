package com.anshuman.workflow.statemachine.util;

import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineEventResult;
import reactor.core.publisher.Flux;

@Slf4j
public class EventResultHelper {

    private EventResultHelper() {
        // use class statically
    }

    public static <S, E> Flux<EventResultDTO<S, E>> toResultDTOFlux(Flux<StateMachineEventResult<S, E>> resultFlux) {
        return resultFlux
            .map(EventResultDTO::new)
            .doOnError(ex -> {
                throw new StateMachineException("Error parsing the results in the state machine.", ex);
            })
            .onErrorStop();
    }

    public static <S, E> List<EventResultDTO<S, E>> toResultDTOList(Flux<StateMachineEventResult<S, E>> resultFlux) {
        List<EventResultDTO<S, E>> eventResults = EventResultHelper
            .toResultDTOFlux(resultFlux)
            .collectList()
            .block();
        log.trace("Parsing StateMachine event results to list: {}", eventResults);
        return eventResults;
    }

    public static <S, E> String toResultDTOString(Flux<StateMachineEventResult<S, E>> resultFlux) {
        return "[" + Optional
            .ofNullable(toResultDTOList(resultFlux))
            .or(() -> Optional.of(Collections.emptyList()))
            .stream()
            .flatMap(Collection::stream)
            .map(EventResultDTO::toString)
            .collect(Collectors.joining(",\n")) + "]";
    }

    public static <S, E> List<EventResultDTO<S, E>> processResultFlux(Flux<StateMachineEventResult<S, E>> resultFlux) {
        // parse the result
        List<EventResultDTO<S, E>> resultDTOList = EventResultHelper.toResultDTOList(resultFlux);
        log.debug("resultFlux is: {}", resultDTOList);

        // empty result
        if (resultDTOList == null || resultDTOList.isEmpty()) {
            log.warn("state machine event result was empty" );
            return Collections.emptyList();
        }

        // find out if any event wasn't accepted.
        boolean hasErrors = resultDTOList.stream().anyMatch(Predicate.not(EventResultDTO.accepted));

        // throw error if the event is not accepted by the state machine.
        if (hasErrors) {
            String eventStr = resultDTOList
                .stream()
                .filter(Predicate.not(EventResultDTO.accepted))
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
