package com.anshuman.workflow.resource.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseDto {

    // base entity
    Long companyId;
    Integer branchId;
    LocalDateTime createDate;
    LocalDateTime updateDate;
    LocalDateTime deleteDate;
}
