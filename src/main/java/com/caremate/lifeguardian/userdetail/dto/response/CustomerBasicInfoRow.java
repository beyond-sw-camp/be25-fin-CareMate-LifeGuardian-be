package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

@Getter
@Setter
@Alias("BasicInfo")
public class CustomerBasicInfoRow {

    private Long customerId;
    private String childName;
    private String childGender;
    private Integer childAge;
    private LocalDate childBirthDate;
    private String consultStatusCode;
    private String consultStatusName;
    private String conversionStatusCode;
    private String conversionStatusName;
    private String reportUrl;
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
}
