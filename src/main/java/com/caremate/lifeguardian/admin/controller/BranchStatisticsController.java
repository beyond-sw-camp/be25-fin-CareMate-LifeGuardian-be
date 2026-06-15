package com.caremate.lifeguardian.admin.controller;

import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;
import com.caremate.lifeguardian.admin.dto.response.BranchMonthlyContractsResponse;
import com.caremate.lifeguardian.admin.dto.response.BranchSalesRankingResponse;
import com.caremate.lifeguardian.admin.dto.response.SalesUserPersonalPerformanceResponse;
import com.caremate.lifeguardian.admin.service.BranchStatisticsService;
import com.caremate.lifeguardian.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.caremate.lifeguardian.admin.dto.response.DashboardSalesUsersResponse;
import com.caremate.lifeguardian.common.exception.ErrorResponse;
import java.util.List;

@Tag(name = "지점 통계 API", description = "지점 실적 및 계약 통계를 조회하는 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchStatisticsController {

    private final BranchStatisticsService branchStatisticsService;

    @Operation(summary = "지점 연간 누적 계약 통계 조회", description = "지점의 연간 누적 계약 건수, 전년 대비 증감률, 연간 목표 건수 및 달성률을 조회합니다.")
    @GetMapping("/{branchId}/statistics/annual-contracts")
    public ResponseEntity<ApiResponse<BranchAnnualContractsResponse>> getBranchAnnualContracts(
            @PathVariable("branchId") Long branchId,
            @RequestParam(value = "targetYear", required = false) Integer targetYear) {

        log.info("지점 연간 누적 계약 통계 조회 API 요청 수신 - branchId: {}, targetYear: {}", branchId, targetYear);
        BranchAnnualContractsResponse response = branchStatisticsService.getBranchAnnualContracts(branchId, targetYear);
        log.info("지점 연간 누적 계약 통계 조회 API 처리 성공 - branchId: {}, targetYear: {}", branchId, targetYear);

        return ResponseEntity.ok(ApiResponse.success(200, "지점 연간 계약 통계 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "영업사원 월간 당월 계약 통계 조회", description = "이번 달 지점 합산 계약 건수 및 1인당 평균 실적, 전월 대비 증감을 조회합니다.")
    @GetMapping("/{branchId}/statistics/monthly-contracts")
    public ResponseEntity<ApiResponse<BranchMonthlyContractsResponse>> getBranchMonthlyContracts(
            @PathVariable("branchId") Long branchId,
            @RequestParam(value = "targetYearMonth", required = false) String targetYearMonth) {

        log.info("영업사원 월간 당월 계약 통계 조회 API 요청 수신 - branchId: {}, targetYearMonth: {}", branchId, targetYearMonth);
        BranchMonthlyContractsResponse response = branchStatisticsService.getBranchMonthlyContracts(branchId, targetYearMonth);
        log.info("영업사원 월간 당월 계약 통계 조회 API 처리 성공 - branchId: {}, targetYearMonth: {}", branchId, targetYearMonth);

        return ResponseEntity.ok(ApiResponse.success(200, "월간 계약 통계 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "지점 월간 판매 실적 상/하위 랭킹 차트 조회", description = "지점의 이번 달 실적 상위 3명과 하위 3명 데이터를 조회합니다.")
    @GetMapping("/{branchId}/statistics/sales-ranking")
    public ResponseEntity<ApiResponse<BranchSalesRankingResponse>> getBranchSalesRanking(
            @PathVariable("branchId") Long branchId,
            @RequestParam(value = "targetYearMonth", required = false) String targetYearMonth) {

        log.info("지점 월간 판매 실적 상/하위 랭킹 차트 조회 API 요청 수신 - branchId: {}, targetYearMonth: {}", branchId, targetYearMonth);
        BranchSalesRankingResponse response = branchStatisticsService.getBranchSalesRanking(branchId, targetYearMonth);
        log.info("지점 월간 판매 실적 상/하위 랭킹 차트 조회 API 처리 성공 - branchId: {}, targetYearMonth: {}", branchId, targetYearMonth);

        return ResponseEntity.ok(ApiResponse.success(200, "상/하위 영업사원 성과 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "영업사원 개인 판매 실적 상세 조회", description = "특정 영업사원의 이번 달 실적, 연간 누적, 월별 트렌드 및 지점 내 랭킹 등을 조회합니다.")
    @GetMapping("/{branchId}/statistics/sales-users/{targetUserId}/performance")
    public ResponseEntity<ApiResponse<SalesUserPersonalPerformanceResponse>> getSalesUserPersonalPerformance(
            @PathVariable("branchId") Long branchId,
            @PathVariable("targetUserId") Long targetUserId) {

        log.info("영업사원 개인 판매 실적 상세 조회 API 요청 수신 - branchId: {}, targetUserId: {}", branchId, targetUserId);
        SalesUserPersonalPerformanceResponse response = branchStatisticsService.getSalesUserPersonalPerformance(branchId, targetUserId);
        log.info("영업사원 개인 판매 실적 상세 조회 API 처리 성공 - branchId: {}, targetUserId: {}", branchId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(200, "영업사원 개인 상세 실적 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "대시보드 영업사원 목록 조회", description = "로그인한 관리자가 지정한 지점의 영업사원 목록을 핀 고정 및 당월 실적 기준으로 정렬하여 조회합니다.")
    @GetMapping("/{branchId}/dashboard/sales-users")
    public ResponseEntity<ApiResponse<?>> getDashboardSalesUsers(
            @PathVariable("branchId") Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("대시보드 영업사원 목록 조회 API 요청 수신 - branchId: {}, keyword: {}", branchId, keyword);

        // 검색어 형식 수동 유효성 검증
        if (keyword != null) {
            if (keyword.length() > 20 || !keyword.matches("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s]*$")) {
                List<ErrorResponse> errors = List.of(
                        ErrorResponse.builder()
                                .field("keyword")
                                .reason("검색어는 특수문자를 제외한 20자 이내로 입력해주세요.")
                                .build()
                );
                return ResponseEntity
                        .status(400)
                        .body(ApiResponse.fail(400, "검색어 형식이 올바르지 않습니다.", errors));
            }
        }

        DashboardSalesUsersResponse response = branchStatisticsService.getDashboardSalesUsers(branchId, keyword);
        log.info("대시보드 영업사원 목록 조회 API 처리 성공 - branchId: {}, 조회 건수: {}", branchId, response.getTotalCount());

        return ResponseEntity.ok(ApiResponse.success(200, "대시보드 영업사원 목록 조회가 완료되었습니다.", response));
    }
}
