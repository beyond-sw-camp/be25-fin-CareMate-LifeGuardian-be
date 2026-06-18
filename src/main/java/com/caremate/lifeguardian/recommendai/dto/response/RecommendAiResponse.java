package com.caremate.lifeguardian.recommendai.dto.response;

import com.caremate.lifeguardian.recommendai.dto.CoverageDto;
import com.caremate.lifeguardian.recommendai.dto.ScoreDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendAiResponse {
    private Long customerId;
    private String conversionStatusCode;
    private Long webformResponseId;
    private Long recommendationId;
    private Long insurancePlanId;
    private String recommendationTypeCode;
    private String recommendationTypeName;
    private String planName;
    private String recommendedCategoryCode;
    private String recommendedCategoryName;
    private String desiredBudgetCode;
    private String budgetRangeName;
    private Integer totalPremium;
    private String recommendReason;
    private List<CoverageDto> coverages;
    private List<ScoreDetailDto> scoreDetails;
    private String scriptContent;
    private String createdAt;
}
