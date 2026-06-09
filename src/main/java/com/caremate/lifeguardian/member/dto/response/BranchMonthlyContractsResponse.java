package com.caremate.lifeguardian.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
// (불변 객체 및 Redis 역직렬화 지원)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BranchMonthlyContractsResponse {
    int currentMonthCount;
    int previousMonthCount;
    int momDifferenceCount;
    int activeSalesUserCount;
    double averagePerUser;
}
