package com.caremate.lifeguardian.dashboard.mapper;

import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
