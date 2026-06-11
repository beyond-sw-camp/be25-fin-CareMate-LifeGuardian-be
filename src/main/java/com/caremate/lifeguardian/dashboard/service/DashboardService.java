package com.caremate.lifeguardian.dashboard.service;

import com.caremate.lifeguardian.dashboard.dto.response.DashboardAchievementResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;

public interface DashboardService {

    /**
     * 대시보드 요약 조회
     *
     * @return 로그인한 영업사원의 대시보드 요약 정보
     */
    DashboardSummaryResponse getDashboardSummary();

    /**
     * 영업 달성률 조회
     *
     * @return 로그인한 영업사원의 이번 달 영업 달성률
     */
    DashboardAchievementResponse getDashboardAchievement();
}
