package com.caremate.lifeguardian.dashboard.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.dashboard.dto.response.ContactCustomerResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardAchievementResponse;
import com.caremate.lifeguardian.dashboard.dto.response.DashboardSummaryResponse;
import com.caremate.lifeguardian.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 영업 달성률 조회 API
     *
     * 기능:
     * - 로그인한 영업사원의 이번 달 영업 목표 대비 계약 완료 건수와 달성률을 조회한다.
     *
     * 조회 데이터:
     * - 이번 달 목표 계약 건수
     * - 이번 달 계약 완료 건수
     * - 영업 달성률
     *
     * 현재는 SecurityUtil에서 테스트용 사용자 ID를 가져온다.
     * 추후 JWT 적용 시 SecurityUtil 내부 로직만 실제 로그인 사용자 추출 방식으로 변경하면 된다.
     *
     * @return 영업 달성률 정보
     */
    @Operation(summary = "영업 달성률 조회", description = "로그인한 영업사원의 이번달 목표 계약 건수, 계약 완료 건수, 달성률을 조회합니다.")
    @GetMapping("/achievement")
    public ApiResponse<DashboardAchievementResponse> getDashboardAchievement() {

        DashboardAchievementResponse response =
                dashboardService.getDashboardAchievement();

        return ApiResponse.success(
                200,
                "영업 달성률 조회에 성공했습니다.",
                response
        );
    }

    /**
     * 오늘 연락 고객 목록 조회 API
     *
     * 기능:
     * - 로그인한 영업사원이 오늘 연락해야 하는 잠재고객 목록을 조회한다.
     *
     * 조회 조건:
     * - 오늘 생일인 잠재고객
     * - 상령일 D-30인 잠재고객
     * - 상령일 D-7인 잠재고객
     * - 상령일 D-DAY인 잠재고객
     * - 3step Case A(가족 통합 리모델링) 대상 잠재고객
     *
     * 버튼 제어:
     * - 생일인 고객은 웹폼 발송 버튼 활성화
     * - 상령일 D-DAY 고객은 리포트 발송 버튼 활성화
     *
     * 현재는 SecurityUtil에서 테스트용 사용자 ID를 가져온다.
     * 추후 JWT 적용 시 SecurityUtil 내부 로직만 실제 로그인 사용자 추출 방식으로 변경하면 된다.
     *
     * @return 오늘 연락 고객 목록
     */
    @Operation(summary = "오늘 연락 고객 목록 조회", description = "로그인한 영업사원이 오늘 연락해야 하는 잠재고객 목록을 조회합니다.")
    @GetMapping("/contact-customers")
    public ApiResponse<List<ContactCustomerResponse>> getTodayContactCustomers() {

        List<ContactCustomerResponse> response =
                dashboardService.getTodayContactCustomers();

        return ApiResponse.success(
                200,
                "오늘 연락 고객 목록 조회에 성공했습니다.",
                response
        );
    }
}
