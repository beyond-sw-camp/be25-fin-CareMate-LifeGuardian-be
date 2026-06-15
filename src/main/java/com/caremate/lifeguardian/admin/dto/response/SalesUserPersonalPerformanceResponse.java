package com.caremate.lifeguardian.admin.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SalesUserPersonalPerformanceResponse {
    String employeeName;
    String positionName;
    int thisMonthCount;
    int annualCount;
    int monthlyTargetCount;
    int targetDifference;
    List<SalesUserMonthlyTrend> monthlyTrends;
    int previousMonthCount;
    int momDifference;
    double targetAchievementRate;
    int branchRank;
    int totalBranchUsers;
    boolean isTargetAchieved;
}
