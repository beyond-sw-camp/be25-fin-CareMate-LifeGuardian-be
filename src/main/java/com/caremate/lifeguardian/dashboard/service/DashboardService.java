package com.caremate.lifeguardian.dashboard.service;

import com.caremate.lifeguardian.dashboard.dto.response.ContactCustomerResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardAchievementResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;

import java.util.List;

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

    /**
     * 오늘 연락 고객 목록 조회
     *
     * @return 로그인한 영업사원의 오늘 연락해야 할 잠재고객 목록
     */
    List<ContactCustomerResponse> getTodayContactCustomers();
}
