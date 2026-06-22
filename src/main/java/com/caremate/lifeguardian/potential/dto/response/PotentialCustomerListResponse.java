package com.caremate.lifeguardian.potential.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PotentialCustomerListResponse {

    private Long potentialCustomerId;
    private String customerName;
    private String gender;
    private Integer age;  // 자녀 만 나이
    private LocalDate birthDate;

    private String guardianName;
    private String guardianRelationshipCode;
    private String guardianRelationshipName;
    private Integer guardianAge;  // 부모 만 나이
    private String guardianPhone;

    private Boolean isActive;  // 잠재고객 졸업 여부 / false=졸업 전, true=졸업

    private LocalDateTime createdAt;
}
