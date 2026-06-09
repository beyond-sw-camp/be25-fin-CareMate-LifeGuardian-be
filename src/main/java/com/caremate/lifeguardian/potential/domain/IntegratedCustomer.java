package com.caremate.lifeguardian.potential.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class IntegratedCustomer {

    private Long integratedCustomerId;
    private Long parentId;
    private Long salesUserId;

    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String rrnEncrypted;
    private String address;

    private String lifecycleCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate insuranceAgeShiftDate;
}
