package com.caremate.lifeguardian.potential.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PotentialCustomer {

    private Long id;
    private Long parentCustomerId;
    private Long salesUserId;
    private String relationshipCode;

    private String name;
    private String gender;
    private LocalDate birthDate;

    private String consultStatusCode;
    private String conversionStatusCode;
    private LocalDate insuranceAgeShiftDate;
    private LocalDate webformReceivedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
