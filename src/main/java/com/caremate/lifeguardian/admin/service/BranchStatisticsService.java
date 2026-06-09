package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;
import com.caremate.lifeguardian.member.dto.response.BranchMonthlyContractsResponse;

public interface BranchStatisticsService {
    // 지점 연간 누적 계약 통계 조회
    BranchAnnualContractsResponse getBranchAnnualContracts(Long branchId, Integer targetYear);

    // 지점 월간 당월 계약 통계 조회
    BranchMonthlyContractsResponse getBranchMonthlyContracts(Long branchId, String targetYearMonth);

}
