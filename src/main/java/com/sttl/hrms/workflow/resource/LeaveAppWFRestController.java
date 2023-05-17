package com.sttl.hrms.workflow.resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.sttl.hrms.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.sttl.hrms.workflow.exception.WorkflowException;
import com.sttl.hrms.workflow.resource.dto.EventResponseDto;
import com.sttl.hrms.workflow.resource.dto.LeaveAppWFInstanceDto;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.service.LeaveAppService;
import com.sttl.hrms.workflow.statemachine.EventResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("wf/leave")
@RequiredArgsConstructor
@Slf4j
@Validated
public class LeaveAppWFRestController {

    private final LeaveAppService leaveAppWFService;

	@PostMapping("/")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ResponseEntity<Object> createLeaveApp(@RequestBody @Valid LeaveAppWFInstanceDto dto) {
		log.debug("create leaveApp Entity from dto: {}", dto);
		if (dto.getCreatedByUserId() == null)
			throw new WorkflowException("Cannot create leave application",
					new IllegalArgumentException("Created By User Id cannot be null"));

		Set<Long> duplicateId = new HashSet<>();
		
		// If duplicate reviwers present at same level then store that id into duplicateId set
		dto.getReviewers().forEach(x -> duplicateId.addAll(getDuplicates(x.getSecond())));
		
		// If In duplicateId set any Id present then return error message
		if (duplicateId.size() != 0)
		return ResponseEntity.ok("Reviwer Id is same");

		return ResponseEntity.ok(leaveAppWFService.create(LeaveAppWFInstanceDto.toEntity(dto)));

	}

    @GetMapping("/{id}")
    public @ResponseBody LeaveAppWorkFlowInstanceEntity getLeaveAppById(@PathVariable("id") Long id) {
        if (!leaveAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return leaveAppWFService.getById(id);
    }

    @GetMapping("/")
    public @ResponseBody List<LeaveAppWorkFlowInstanceEntity> getAllLeaveApps() {
        return leaveAppWFService.getAll();
    }

    @PostMapping("/event")
    public @ResponseBody List<EventResponseDto> sendEventToLeaveAppWF(@NotNull @RequestBody @Valid PassEventDto eventDto) {
        log.debug("update leaveApp Entity with event: {}", eventDto);
        return leaveAppWFService.passEventToSM(eventDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveApp(@PathVariable("id") Long id) {
        if (!leaveAppWFService.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        leaveAppWFService.delete(id);
    }

    @PostMapping("/reset")
    public @ResponseBody List<EventResultDto> resetWorkFlow(@NotNull @RequestBody @Valid PassEventDto eventDto) {
        return leaveAppWFService.resetStateMachine(eventDto);
    }
    
    // For checking duplicate id present in list or not
    public static Set<Long> getDuplicates(List<Long> list) {
	    return list.stream().filter(i -> Collections.frequency(list, i) > 1).collect(Collectors.toSet());
	}

}
