package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.model.entity.LoanAppWorkflowInstanceEntity;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.LoanAppWFInstanceDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.service.LoanAppWFService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("wf/loan")
@RequiredArgsConstructor
@Slf4j
@Validated
public class LoanAppWFRestController {

    private final LoanAppWFService loanAppWFService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody LoanAppWorkflowInstanceEntity createLeaveAppWFInstance(@RequestBody @Valid LoanAppWFInstanceDto dto) {
        log.debug("create leaveAppWfEntity from dto: {}", dto);
        return loanAppWFService.createLeaveApplication(LoanAppWFInstanceDto.toEntity(dto));
    }

    @GetMapping("/{id}")
    public @ResponseBody LoanAppWorkflowInstanceEntity getLeaveAppWFInstanceById(@PathVariable("id") Long id) {
        if (!loanAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return loanAppWFService.getLeaveApplicationById(id);
    }

    @GetMapping("/")
    public @ResponseBody List<LoanAppWorkflowInstanceEntity> getAllLeaveAppWFInstances() {
        return loanAppWFService.getAll();
    }

    @PostMapping("/event")
    public @ResponseBody List<EventResponseDto> sendEvent(@NotNull @RequestBody @Valid PassEventDto eventDto) {
        log.debug("update loanAppWfEntity with event: {}", eventDto);
        return loanAppWFService.passEvent(eventDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveApp(@PathVariable("id") Long id) {
        if (!loanAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        loanAppWFService.deleteLeaveApplication(id);
    }

}
