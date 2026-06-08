package com.caremate.lifeguardian.admin.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BranchAnnualContractsResponse {
    int currentYearCount;
    int previousYearCount;
    double yoyGrowthRate;
    int annualTargetCount;
    double targetAchievementRate;
}
