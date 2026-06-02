package com.caremate.lifeguardian.sales.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import com.caremate.lifeguardian.sales.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/sales/performance")
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponseDto>> getSalesSummary() {
        SalesSummaryResponseDto response = salesService.getSalesSummary();

        return ResponseEntity.ok(
                ApiResponse.success(200, "영업현황 요약 조회에 성공했습니다.", response)
        );
    }


}
