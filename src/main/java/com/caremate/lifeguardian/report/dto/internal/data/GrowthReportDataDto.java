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
public class GrowthReportDataDto {

    private ReportCustomerInfoDto customer;
    private ReportWebformDto webform;
    private List<GrowthStandardDto> growthStandards;
    private List<DiseaseRiskItemDto> currentRisks;
    private List<DiseaseRiskItemDto> nextRisks;
    private String currentAgeGroupName;
    private String nextAgeGroupName;
    private String heightSummary;
    private String weightSummary;
    private String overallSummary;
}
