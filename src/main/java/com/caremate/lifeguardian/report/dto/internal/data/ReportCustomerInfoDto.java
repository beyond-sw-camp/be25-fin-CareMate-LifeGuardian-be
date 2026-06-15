package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("ReportCustomerInfoDto")
public class ReportCustomerInfoDto {

    private Long customerId;
    private String childName;
    private LocalDate childBirthDate;
    private Integer childAge;
    private Integer childAgeMonth;
    private Long parentCustomerId;
    private String parentName;
    private Integer parentAge;
    private String parentPhone;
    private String parentAddress;
    private String childGender;
    private String relationCode;
    private String relationName;
}
