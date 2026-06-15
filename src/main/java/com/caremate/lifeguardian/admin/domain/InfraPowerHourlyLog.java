package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfraPowerHourlyLog {
    private Long id;
    private LocalDate logDate;
    private Integer logHour;
    private Double traditionalEstimatedCpuUtil;
    private Double optimizedActualCpuUtil;
    private Double powerConsumptionKw;
    private LocalDateTime createdAt;
}
