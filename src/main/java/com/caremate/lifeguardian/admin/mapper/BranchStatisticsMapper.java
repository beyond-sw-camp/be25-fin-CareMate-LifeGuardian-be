package com.caremate.lifeguardian.admin.mapper;

import com.caremate.lifeguardian.admin.domain.DashboardSalesUser;
import com.caremate.lifeguardian.admin.domain.SalesUserMonthlyTrendDto;
import com.caremate.lifeguardian.admin.domain.SalesUserPerformance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BranchStatisticsMapper {

    // 특정 지점의 연간 계약 수 카운트
    int countContractsByBranchAndYear(@Param("branchId") Long branchId, @Param("year") int year);

    // 특정 지점의 연간 목표치 조회 (소속 영업사원들의 월간 목표 합산)
    Integer selectAnnualTarget(@Param("branchId") Long branchId, @Param("year") int year);

    // 특정 지점의 월별 계약 수 카운트
    int countContractsByBranchAndMonth(@Param("branchId") Long branchId, @Param("yearMonth") String yearMonth);

    // 특정 지점의 활성 영업사원 수 조회
    int countActiveSalesUsersByBranch(@Param("branchId") Long branchId);

    // 특정 지점의 월간 영업사원 실적 및 랭킹 조회
    List<SalesUserPerformance> selectSalesUsersPerformanceRanking(
            @Param("branchId") Long branchId,
            @Param("yearMonth") String yearMonth
    );

    // 직급 코드에 해당하는 직급명 조회
    String selectPositionName(@Param("rankCode") String rankCode);

    // 특정 영업사원의 월간 목표 실적 조회
    Integer selectSalesUserMonthlyTarget(
            @Param("salesUserId") Long salesUserId,
            @Param("yearMonth") String yearMonth
    );

    // 특정 영업사원의 월간 계약 건수 조회
    int countSalesUserContractsByMonth(
            @Param("salesUserId") Long salesUserId,
            @Param("yearMonth") String yearMonth
    );

    // 특정 영업사원의 연간 계약 건수 조회
    int countSalesUserContractsByYear(
            @Param("salesUserId") Long salesUserId,
            @Param("year") int year
    );

    // 특정 영업사원의 시작 일시 이후 월간 실적 추이 조회
    List<SalesUserMonthlyTrendDto> selectSalesUserMonthlyTrends(
            @Param("salesUserId") Long salesUserId,
            @Param("startDateTime") String startDateTime
    );

    // 특정 지점 내 특정 영업사원의 월간 실적 랭킹 조회
    Integer selectSalesUserBranchRank(
            @Param("branchId") Long branchId,
            @Param("salesUserId") Long salesUserId,
            @Param("yearMonth") String yearMonth
    );

    // 대시보드에 표시할 영업사원 목록 조회
    List<DashboardSalesUser> selectDashboardSalesUsers(
            @Param("branchId") Long branchId,
            @Param("managerUserId") Long managerUserId,
            @Param("yearMonth") String yearMonth,
            @Param("keyword") String keyword
    );

    // 매니저의 특정 영업사원 핀 고정(즐겨찾기) 추가
    int insertPin(@Param("managerUserId") Long managerUserId, @Param("targetSalesUserId") Long targetSalesUserId);

    // 매니저의 특정 영업사원 핀 고정(즐겨찾기) 해제
    int deletePin(@Param("managerUserId") Long managerUserId, @Param("targetSalesUserId") Long targetSalesUserId);

    // 매니저의 특정 영업사원 핀 고정(즐겨찾기) 등록 여부 확인
    boolean existsPin(@Param("managerUserId") Long managerUserId, @Param("targetSalesUserId") Long targetSalesUserId);
}
