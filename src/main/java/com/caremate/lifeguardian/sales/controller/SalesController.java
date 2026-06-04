package com.caremate.lifeguardian.sales.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import com.caremate.lifeguardian.sales.service.SalesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponseDto>> getSalesSummary(
            // 요청값 형식 검증
            @RequestParam @Positive Long salesUserId,
            @RequestParam @Pattern(regexp = "\\d{6}", message = "조회 연월은 yyyyMM 형식이어야 합니다.") String targetYearMonth
            ) {
        SalesSummaryResponseDto response = salesService.getSalesSummary(salesUserId, targetYearMonth);

        return ResponseEntity.ok(
                ApiResponse.success(200, "영업현황 요약 조회에 성공했습니다.", response)
        );
    }
}
