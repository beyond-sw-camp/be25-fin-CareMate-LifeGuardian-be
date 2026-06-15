package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.response.*;

public interface BranchStatisticsService {
    // 지점 연간 누적 계약 통계를 조회합니다.
    BranchAnnualContractsResponse getBranchAnnualContracts(Long branchId, Integer targetYear);

    // 지점 월간 당월 계약 통계를 조회합니다.
    BranchMonthlyContractsResponse getBranchMonthlyContracts(Long branchId, String targetYearMonth);

    // 지점 월간 판매 실적 상/하위 랭킹을 조회합니다.
    BranchSalesRankingResponse getBranchSalesRanking(Long branchId, String targetYearMonth);

    // 영업사원 개인 판매 실적 상세 정보를 조회합니다.
    SalesUserPersonalPerformanceResponse getSalesUserPersonalPerformance(Long branchId, Long targetUserId);

    // 대시보드용 영업사원 목록을 조회합니다.
    DashboardSalesUsersResponse getDashboardSalesUsers(Long branchId, String keyword);

    // 영업사원을 대시보드에 핀 고정합니다.
    void pinSalesUser(Long targetUserId);

    // 영업사원의 대시보드 핀 고정을 해제합니다.
    void unpinSalesUser(Long targetUserId);
}
