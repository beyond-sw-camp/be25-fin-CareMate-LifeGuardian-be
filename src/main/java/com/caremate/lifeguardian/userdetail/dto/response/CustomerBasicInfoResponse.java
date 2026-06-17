package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CustomerBasicInfoResponse {

    private Long customerId;
    private String conversionStatusCode;
    private String conversionStatusName;
    private String reportUrl;

    private String childName;
    private String childGender;
    private Integer childAge;
    private LocalDate childBirthDate;

    private String consultStatusCode;
    private String consultStatusName;

    private String lifeStageCode;
    private String lifeStageName;
    private LocalDate insuranceAgeShiftDate;

    private Long parentCustomerId;
    private String guardianName;
    private String relationshipCode;
    private String relationshipName;
    private String guardianPhone;
    private String guardianAddress;
    private Integer guardianAge;

    private CustomerBasicInfoAlert alert;
    private List<CustomerBasicInfoBadge> badges;
}
