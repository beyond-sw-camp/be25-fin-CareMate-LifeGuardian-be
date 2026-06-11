package com.caremate.lifeguardian.dashboard.service;

import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;
import com.caremate.lifeguardian.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    /**
     * 대시보드 요약 조회 실제 구현
     *
     * 처리 흐름:
     * - SecurityUtil에서 현재 로그인한 영업사원 ID를 가져온다.
     * - 해당 영업사원이 담당하는 잠재고객/계약 상태별 건수를 조회한다.
     * - 조회 결과를 Controller로 반환한다.
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        return dashboardMapper.findDashboardSummary(salesUserId);
    }
}
