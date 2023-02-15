package com.anshuman.workflow.data.enums;

import lombok.Getter;

@Getter
public enum LeaveType {
    EL(1, "Earned Leave"),
    LND(2, "Leave Not Due"),
    ML(3, "Maternity Leave"),
    SCL(4, "Special Casual Leave"),
    CL(5, "Casual Leave"),
    LHP(6, "Leave on Half Pay"),
    CML(7, "Commuted Leave"),
    EOL(8, "Extra Ordinary Leave"),
    SDL(9, "Special Disability Leave"),
    RH(10, "Restricted Holiday"),
    SEL(11, "Special Election Leave"),
    CCL(12, "Child Care Leave"),
    MML(13, "Miscarriage Leave"),
    PL(14, "Paternity Leave"),
    CAL(15, "Child Adoption Leave");

    @Getter
    private final String leaveName;
    private final int number;
    private static final LeaveType[] values = LeaveType.values();

    LeaveType(int number, String leaveName) {
        this.leaveName = leaveName;
        this.number = number;
    }

    public static LeaveType fromNumber(int number) {
        for(LeaveType lt : values) {
            if (lt.getNumber() == number)
                return lt;
        }
        throw new IllegalArgumentException("No leave type found for the specified number");
    }
}
