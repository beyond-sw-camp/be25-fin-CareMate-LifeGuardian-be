package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;

public interface BranchStatisticsService {
    // 지점 연간 누적 계약 통계 조회
    BranchAnnualContractsResponse getBranchAnnualContracts(Long branchId, Integer targetYear);

}
