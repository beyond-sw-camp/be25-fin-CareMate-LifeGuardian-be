package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfraEsgCumulativeMetric {
    private Integer id;
    private Double totalSavedCarbonKg;
    private Double totalSavedPowerKwh;
    private Long totalSavedCostKrw;
    private LocalDateTime updatedAt;
}
