package com.caremate.lifeguardian.userdetail.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.userdetail.dto.response.CustomerBasicInfoResponse;
import com.caremate.lifeguardian.userdetail.service.CustomerDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고객 상세 API", description = "고객 상세 페이지를 통해 고객 정보, 보험추천, 스크립트를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerDetailController {

    private final CustomerDetailService customerDetailService;

    @Operation(summary = "고객 기본정보 조회", description = "담당 잠재고객 또는 통합고객의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/{customerId}/detail")
    public ApiResponse<CustomerBasicInfoResponse> getCustomerBasicInfo(
            @PathVariable Long customerId,
            @RequestParam String conversionStatusCode
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        CustomerBasicInfoResponse response =
                customerDetailService.getCustomerBasicInfo(
                        customerId,
                        conversionStatusCode,
                        currentUserId
                );

        return ApiResponse.success(200, "고객 기본정보 조회에 성공했습니다.", response);
    }
}
