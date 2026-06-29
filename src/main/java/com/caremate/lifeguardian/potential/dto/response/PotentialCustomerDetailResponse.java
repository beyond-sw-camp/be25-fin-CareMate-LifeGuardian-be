package com.caremate.lifeguardian.potential.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PotentialCustomerDetailResponse {

    private Long potentialCustomerId;

    // 부모 정보
    private Long parentCustomerId;
    private String parentName;
    private LocalDate parentBirthDate;
    private String parentPhone;
    private String parentAddress;
    private String relationshipCode;
    private String relationshipName;

    // 잠재고객 정보
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Integer age;

    private String consultStatusCode;
    private String consultStatusName;
    private String conversionStatusCode;
    private String conversionStatusName;
}
