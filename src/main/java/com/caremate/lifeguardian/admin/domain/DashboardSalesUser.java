package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSalesUser {
    private Long userId;
    private String employeeName;
    private int rank;
    private int thisMonthCount;
    private int targetDifference;

    @JsonProperty("isPinned")
    private boolean isPinned;
}
