package com.anshuman.workflow.data.dto;

import java.time.LocalDateTime;
import lombok.Value;

@Value
public class BaseDto {

    // base entity
    Long companyId;
    Integer branchId;
    LocalDateTime createDate;
    LocalDateTime updateDate;
    LocalDateTime deleteDate;
}
