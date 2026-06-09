package com.caremate.lifeguardian.sales.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.sales.dto.request.SalesSearchRequestDto;
import com.caremate.lifeguardian.sales.dto.response.SalesPageResponseDto;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import com.caremate.lifeguardian.sales.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "영업현황 KPI API", description = "영업현황 페이지 KPI 조회 API입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/sales/performance")
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "영업사원 KPI 조회", description = "영업사원의 이달 성과 및 목표성과를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponseDto>> getSalesSummary(
            // 조회 대상 연월은 yyyyMM 형식으로 받는다.
            @RequestParam @Pattern(regexp = "\\d{6}", message = "조회 연월은 yyyyMM 형식이어야 합니다.") String targetYearMonth
    ) {
        // 로그인한 영업사원의 KPI만 조회한다.
        Long currentUserId = SecurityUtil.getCurrentUserId();

        SalesSummaryResponseDto response = salesService.getSalesSummary(currentUserId, targetYearMonth);

        return ResponseEntity.ok(
                ApiResponse.success(200, "영업현황 요약 조회에 성공했습니다.", response)
        );
    }

    @Operation(summary = "영업현황 목록 조회", description = "영업사원이 관리하는 고객의 영업현황 목록을 조회합니다.")
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<SalesPageResponseDto>> getSalesList(@ModelAttribute SalesSearchRequestDto request) {
        // 요청 파라미터의 salesUserId는 신뢰하지 않고 로그인 사용자 기준으로 조회한다.
        Long currentUserId = SecurityUtil.getCurrentUserId();

        SalesPageResponseDto response = salesService.getSalesList(currentUserId, request);

        return ResponseEntity.ok(
                ApiResponse.success(200, "영업현황 목록 조회에 성공했습니다.", response)
        );
    }
}
