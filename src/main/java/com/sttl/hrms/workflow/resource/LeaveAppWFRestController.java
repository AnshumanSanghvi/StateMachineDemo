package com.sttl.hrms.workflow.resource;

import com.sttl.hrms.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.sttl.hrms.workflow.resource.dto.EventResponseDto;
import com.sttl.hrms.workflow.resource.dto.LeaveAppWFInstanceDto;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.service.LeaveAppWFService;
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
@RequestMapping("wf/leave")
@RequiredArgsConstructor
@Slf4j
@Validated
public class LeaveAppWFRestController {

    private final LeaveAppWFService leaveAppWFService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody LeaveAppWorkFlowInstanceEntity createLeaveAppWFInstance(@RequestBody @Valid LeaveAppWFInstanceDto dto) {
        log.debug("create leaveAppWfEntity from dto: {}", dto);
        return leaveAppWFService.createLeaveApplication(LeaveAppWFInstanceDto.toEntity(dto));
    }

    @GetMapping("/{id}")
    public @ResponseBody LeaveAppWorkFlowInstanceEntity getLeaveAppWFInstanceById(@PathVariable("id") Long id) {
        if (!leaveAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return leaveAppWFService.getLeaveApplicationById(id);
    }

    @GetMapping("/")
    public @ResponseBody List<LeaveAppWorkFlowInstanceEntity> getAllLeaveAppWFInstances() {
        return leaveAppWFService.getAll();
    }

    @PostMapping("/event")
    public @ResponseBody List<EventResponseDto> sendEvent(@NotNull @RequestBody @Valid PassEventDto eventDto) {
        log.debug("update leaveAppWfEntity with event: {}", eventDto);
        return leaveAppWFService.passEvent(eventDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveApp(@PathVariable("id") Long id) {
        if (!leaveAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        leaveAppWFService.deleteLeaveApplication(id);
    }

}
