package com.anshuman.workflow.statemachine.util;

import com.anshuman.workflow.statemachine.data.dto.EventResultDTO;
import com.anshuman.workflow.statemachine.exception.StateMachineException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
            .doOnError(ex -> {
                throw new StateMachineException("Error parsing the results in the state machine.", ex);
            })
            .map(EventResultDTO::new);
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

}
