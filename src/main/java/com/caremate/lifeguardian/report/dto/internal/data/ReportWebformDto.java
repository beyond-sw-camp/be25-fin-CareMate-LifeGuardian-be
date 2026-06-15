package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Alias("ReportWebformDto")
public class ReportWebformDto {

    private Long webFormId;
    private BigDecimal height;
    private BigDecimal weight;
    private String selectedPriorityCategory;
    private String historyJson;
    private String activityJson;
    private boolean pastSurgeryOrHospitalization;
    private String desiredBudgetCode;
    private LocalDateTime receivedAt;
}
