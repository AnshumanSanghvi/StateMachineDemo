package com.sttl.hrms.workflow.resource.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseDto {

    // base entity
    @NotNull Long companyId;
    @NotNull Integer branchId;
    LocalDateTime createDate;
    LocalDateTime updateDate;
    LocalDateTime deleteDate;
}
