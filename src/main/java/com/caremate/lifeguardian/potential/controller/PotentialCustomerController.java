package com.caremate.lifeguardian.potential.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import com.caremate.lifeguardian.potential.service.PotentialCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "잠재고객 관리 API", description = "잠재고객 조회, 등록, 삭제 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/potential-customers")
public class PotentialCustomerController {
    private final PotentialCustomerService potentialCustomerService;

    /**
     * 잠재고객 목록 조회 API
     *
     * 기능:
     * - 로그인한 영업사원이 담당하는 잠재고객 목록 조회
     *
     * 현재는 테스트용으로 salesUserId를 직접 받음
     * 추후 JWT 로그인 적용 시 SecurityUtil에서 사용자 ID 추출 예정
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @return 잠재고객 목록
     */

    @Operation(summary = "잠재고객 목록 조회", description = "로그인한 영업사원이 담당하는 잠재고객 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<PotentialCustomerListResponse>> getPotentialCustomers(
            @RequestParam Long salesUserId
    ) {
        List<PotentialCustomerListResponse> response =
                potentialCustomerService.getPotentialCustomers(salesUserId);

        return ApiResponse.success(
                200,
                "잠재고객 목록 조회에 성공했습니다.",
                response
        );
    }
}
