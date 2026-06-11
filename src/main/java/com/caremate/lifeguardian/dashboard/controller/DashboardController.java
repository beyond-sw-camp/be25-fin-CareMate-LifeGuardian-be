package com.caremate.lifeguardian.dashboard.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;
import com.caremate.lifeguardian.dashboard.service.DashboardService;
import com.caremate.lifeguardian.dashboard.service.DashboardServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "영업사원 대시보드 API", description = "영업사원의 대시보드 요약, 영업 달성률, 연락 고객 목록을 조회합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 요약 조회 API
     *
     * 기능:
     * - 로그인한 영업사원의 대시보드 요약 정보를 조회한다.
     *
     * 조회 데이터:
     * - 잠재고객 미상담 고객 수
     * - 잠재고객 상담중 고객 수
     * - 설계중 고객 수
     * - 설계완료 고객 수
     * - 청약중 고객 수
     * - 청약완료 고객 수
     * - 수납완료 고객 수
     * - 계약완료 고객 수
     *
     * 현재는 SecurityUtil에서 테스트용 사용자 ID를 가져온다.
     * 추후 JWT 적용 시 SecurityUtil 내부 로직만 실제 로그인 사용자 추출 방식으로 변경하면 된다.
     *
     * @return 대시보드 요약 정보
     */
    @Operation(summary = "대시보드 요약 조회", description = "로그인한 영업사원의 잠재고객 상담 상태와 계약 진행 상태별 건수를 조회합니다.")
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary() {

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        return ApiResponse.success(
                200,
                "대시보드 요약 조회에 성공했습니다.",
                response
        );
    }
}
