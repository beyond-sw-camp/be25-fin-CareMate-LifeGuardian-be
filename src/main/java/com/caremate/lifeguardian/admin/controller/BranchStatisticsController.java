package com.caremate.lifeguardian.admin.controller;

import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;
import com.caremate.lifeguardian.admin.service.BranchStatisticsService;
import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.member.dto.response.BranchMonthlyContractsResponse;
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


}
