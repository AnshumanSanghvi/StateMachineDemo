package com.anshuman.workflow.resource;

import com.anshuman.workflow.data.model.entity.LeaveAppWorkFlowInstanceEntity;
import com.anshuman.workflow.resource.dto.EventResponseDto;
import com.anshuman.workflow.resource.dto.LeaveAppWFInstanceDto;
import com.anshuman.workflow.resource.dto.PassEventDto;
import com.anshuman.workflow.service.LeaveAppWFService;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wf/leave")
@RequiredArgsConstructor
@Slf4j
public class LeaveAppWFRestController {

    private final LeaveAppWFService leaveAppWFService;

    @PostMapping("/")
    public @ResponseBody LeaveAppWorkFlowInstanceEntity createLeaveAppWFInstance(@RequestBody LeaveAppWFInstanceDto dto) {
        log.debug("create leaveAppWfEntity from dto: {}", dto);
        return leaveAppWFService.createLeaveApplication(LeaveAppWFInstanceDto.toEntity(dto));
    }

    @GetMapping("/{id}")
    public @ResponseBody LeaveAppWorkFlowInstanceEntity getLeaveAppWFInstanceById(@PathVariable("id") Long id) {
        return leaveAppWFService.getLeaveApplicationById(id);
    }

    @GetMapping("/")
    public @ResponseBody List<LeaveAppWorkFlowInstanceEntity> getAllLeaveAppWFInstances() {
        return leaveAppWFService.getAll();
    }

    @PostMapping("/event")
    public @ResponseBody List<EventResponseDto> sendEvent(@NotNull @RequestBody PassEventDto eventDto) {
        log.debug("update leaveAppWfEntity with event: {}", eventDto);
        return leaveAppWFService.passEvent(eventDto);
    }

    @DeleteMapping("/{id}")
    public void deleteLeaveApp(@PathVariable("id") Long id) {
        leaveAppWFService.deleteLeaveApplication(id);
    }

}
