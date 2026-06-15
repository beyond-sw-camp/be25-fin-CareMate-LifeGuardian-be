package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseReportDataDto {

    private ReportCustomerInfoDto customer;
    private List<DiseaseRiskItemDto> currentRisks;
    private List<DiseaseRiskItemDto> nextRisks;
    private String currentAgeGroupName;
    private String nextAgeGroupName;
}
