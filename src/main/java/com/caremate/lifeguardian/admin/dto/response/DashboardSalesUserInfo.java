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
public class DashboardSalesUserInfo {
    Long userId;
    String employeeName;
    int rank;
    int thisMonthCount;
    int targetDifference;
    boolean isPinned;
}
