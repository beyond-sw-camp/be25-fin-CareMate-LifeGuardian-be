package com.caremate.lifeguardian.dashboard.mapper;

import com.caremate.lifeguardian.dashboard.dto.response.ContactCustomerResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardAchievementResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    /**
     * 대시보드 요약 조회
     *
     * 조회 데이터:
     * - 잠재고객 미상담 고객 수
     * - 잠재고객 상담중 고객 수
     * - 계약 진행 상태별 고객 수
     *
     * 조회 기준:
     * - 로그인한 영업사원 ID
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @return 대시보드 요약 정보
     */
    DashboardSummaryResponse findDashboardSummary(
            @Param("salesUserId") Long salesUserId
    );

    /**
     * 영업 달성률 조회
     *
     * @Param salesUserId 로그인 영업사원 ID
     * @return 영업 달성률 정보
     */
    DashboardAchievementResponse findDashboardAchievement(
            @Param("salesUserId") Long salesUserId
    );

    /**
     * 오늘 연락 고객 목록 조회
     *
     * 조회 기준:
     * - 로그인한 영업사원 ID
     * - 오늘 날짜의 미완료 액션 아이템
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @return 오늘 연락해야 할 고객 목록
     */
    List<ContactCustomerResponse> findTodayContactCustomers(
            @Param("salesUserId") Long salesUserId
    );
}
