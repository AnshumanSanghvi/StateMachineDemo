package com.sttl.hrms.workflow.service;

import com.sttl.hrms.workflow.data.model.entity.LoanAppWorkflowInstanceEntity;
import com.sttl.hrms.workflow.data.model.entity.WorkflowInstanceEntity;
import com.sttl.hrms.workflow.data.model.repository.LoanAppWorkflowInstanceRepository;
import com.sttl.hrms.workflow.resource.dto.EventResponseDto;
import com.sttl.hrms.workflow.resource.dto.PassEventDto;
import com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder;
import com.sttl.hrms.workflow.statemachine.persist.StateMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.sttl.hrms.workflow.statemachine.builder.StateMachineBuilder.SMEvent.*;

@Service
@Slf4j
public class LoanAppService extends WorkflowService<LoanAppWorkflowInstanceEntity> {
    private final LoanAppWorkflowInstanceRepository loanAppRepository;

    public LoanAppService(StateMachineService<WorkflowInstanceEntity> stateMachineService, LoanAppWorkflowInstanceRepository loanAppRepository) {
        super(stateMachineService);
        this.loanAppRepository = loanAppRepository;
    }

    public boolean existsById(Long id) {
        return Optional.ofNullable(loanAppRepository.existsByIdAndWFType(id)).orElse(false);
    }

    public LoanAppWorkflowInstanceEntity create(LoanAppWorkflowInstanceEntity entity) {
        LocalDateTime now = entity.getCreatedDate() == null ? LocalDateTime.now() : entity.getCreatedDate();
        Long userId = entity.getCreatedByUserId();
        entity.setCreatedDate(now);

        List<PassEventDto> passEvents = List.of(
                PassEventDto.builder().workflowType(entity.getTypeId()).event(E_CREATE.name()).actionBy(userId).actionDate(now).build(),
                PassEventDto.builder().workflowType(entity.getTypeId()).event(E_SUBMIT.name()).actionBy(userId).actionDate(now).build(),
                PassEventDto.builder().workflowType(entity.getTypeId()).event(E_TRIGGER_REVIEW_OF.name()).actionBy(userId).actionDate(now).build(),
                PassEventDto.builder().workflowType(entity.getTypeId()).event(E_TRIGGER_FLOW_JUNCTION.name()).actionBy(userId).actionDate(now).build()
        );
        return createApplication(loanAppRepository, entity, passEvents);
    }

    public LoanAppWorkflowInstanceEntity getById(Long id) {
        return getApplicationById(id, loanAppRepository);
    }

    public List<LoanAppWorkflowInstanceEntity> getAll() {
        return getAll(loanAppRepository);
    }

    public List<EventResponseDto> passEventToSM(PassEventDto passEvent) {
        StateMachineBuilder.SMEvent smEvent = StateMachineBuilder.SMEvent.getByName(passEvent.getEvent());
        return switch (smEvent) {
            case E_CREATE -> {
                {
                    List<PassEventDto> passEvents = PassEventDto.createPassEvents(passEvent, E_SUBMIT, E_TRIGGER_REVIEW_OF,
                            E_TRIGGER_FLOW_JUNCTION);
                    yield passEvents(passEvents, loanAppRepository);
                }
            }
            case E_SUBMIT -> {
                List<PassEventDto> passEvents = PassEventDto.createPassEvents(passEvent, E_TRIGGER_REVIEW_OF, E_TRIGGER_FLOW_JUNCTION);
                yield passEvents(passEvents, loanAppRepository);
            }
            case E_TRIGGER_REVIEW_OF -> {
                List<PassEventDto> passEvents = PassEventDto.createPassEvents(passEvent, E_TRIGGER_FLOW_JUNCTION);
                yield passEvents(passEvents, loanAppRepository);
            }
            case E_APPROVE, E_REJECT, E_CANCEL -> {
                List<PassEventDto> passEvents = PassEventDto.createPassEvents(passEvent, E_TRIGGER_COMPLETE);
                yield passEvents(passEvents, loanAppRepository);
            }
            case E_REQUEST_CHANGES_IN, E_TRIGGER_FLOW_JUNCTION, E_FORWARD, E_ROLL_BACK, E_TRIGGER_COMPLETE ->
                    passEvent(passEvent, loanAppRepository);
        };
    }

    public void delete(Long id) {
        deleteApplication(id, loanAppRepository);
    }

}
