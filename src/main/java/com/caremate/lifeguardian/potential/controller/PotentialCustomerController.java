package com.caremate.lifeguardian.potential.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerCreateRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerCreateResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import com.caremate.lifeguardian.potential.service.PotentialCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    /**
     * 부모 통합고객 조회 API
     *
     * 기능:
     * - 잠재고객 등록 전에 부모 통합고객 존재 여부를 확인한다.
     * - 입력한 부모 정보와 integrated_customer 정보를 비교 조회한다.
     *
     * 조회 조건:
     * - 이름
     * - 생년월일
     * - 관계(부/모)
     * - 연락처
     * - 주민등록번호
     * - 주소
     *
     * 처리 방식:
     * - relationshipCode(01=부, 02=모)는 Service에서 gender(MALE/FEMALE)로 변환 후 조회한다.
     *
     * @param request 부모 통합고객 조회 요청 정보
     * @return 부모 통합고객 정보
     */
    @Operation(summary = "부모 통합고객 조회", description = "잠재고객 등록 전 부모 통합고객 존재 여부를 조회하는 API입니다.")
    @PostMapping("/parent/search")
    public ApiResponse<ParentCustomerSearchResponse> findParentCustomer(
            @Valid @RequestBody ParentCustomerSearchRequest request
    ) {
        ParentCustomerSearchResponse response =
                potentialCustomerService.findParentCustomer(request);

        return ApiResponse.success(
                200,
                "부모 통합고객 정보 조회에 성공했습니다.",
                response
        );
    }

    /**
     * 잠재고객 등록 API
     *
     * 기능:
     * - 부모 통합고객과 연결된 자녀 잠재고객을 등록한다.
     *
     * 처리 흐름:
     * - 부모 통합고객 존재 여부 확인
     * - 잠재고객 정보 저장
     * - 등록 완료된 잠재고객 정보 반환
     *
     * 현재는 테스트용으로 salesUserId를 직접 받음
     * 추후 JWT 로그인 적용 시 SecurityUtil에서 사용자 ID 추출 예정
     *
     * @param request 잠재고객 등록 요청 정보
     * @param salesUSerId 로그인한 영업사원 ID
     * @return 등록 완료된 잠재고객 정보
     */
    @Operation(summary = "잠재고객 등록", description = "부모 통합고객과 연결된 자녀 잠재고객을 등록하는 API입니다.")
    @PostMapping
    public ApiResponse<PotentialCustomerCreateResponse> createPotentialCustomer(
            @Valid @RequestBody PotentialCustomerCreateRequest request,
            @RequestParam Long salesUSerId
    ) {
        PotentialCustomerCreateResponse response =
                potentialCustomerService.createPotentialCustomer(
                        request,
                        salesUSerId
                );

        return ApiResponse.success(
                201,
                "잠재고객 등록에 성공했습니다.",
                response
        );
    }
}
