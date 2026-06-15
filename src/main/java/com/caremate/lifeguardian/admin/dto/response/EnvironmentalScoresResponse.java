package com.caremate.lifeguardian.admin.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnvironmentalScoresResponse {
    double totalSavedCarbonKg;
    long totalSavedCostKrw;
}
