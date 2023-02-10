package com.anshuman.workflow.data.enums;

import lombok.Getter;

public enum LeaveType {
    EL("Earned Leave"),
    LND("Leave Not Due"),
    ML("Maternity Leave"),
    SCL("Special Casual Leave"),
    CL("Casual Leave"),
    LHP("Leave on Half Pay"),
    CML("Commuted Leave"),
    EOL("Extra Ordinary Leave"),
    SDL("Special Disability Leave"),
    RH("Restricted Holiday"),
    SEL("Special Election Leave"),
    CCL("Child Care Leave"),
    MML("Miscarriage Leave"),
    PL("Paternity Leave"),
    CAL("Child Adoption Leave");

    @Getter
    private final String leaveName;

    LeaveType(String leaveName) {
        this.leaveName = leaveName;
    }
}
