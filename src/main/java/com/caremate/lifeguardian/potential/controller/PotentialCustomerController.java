package com.caremate.lifeguardian.potential.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerCreateRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerUpdateRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerCreateResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerDeleteResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerDetailResponse;
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
     * @return 잠재고객 목록
     */
    @Operation(summary = "잠재고객 목록 조회", description = "로그인한 영업사원이 담당하는 잠재고객 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<PotentialCustomerListResponse>> getPotentialCustomers() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        List<PotentialCustomerListResponse> response =
                potentialCustomerService.getPotentialCustomers(salesUserId);

        return ApiResponse.success(
                200,
                "잠재고객 목록 조회에 성공했습니다.",
                response
        );
    }

    /**
     * 잠재고객 상세 조회 API
     *
     * 기능:
     * - 잠재고객 목록에서 고객명을 클릭하면
     *   부모 정보와 잠재고객 정보를 함께 조회한다.
     *
     * 처리 흐름:
     * - 로그인한 영업사원 ID 조회
     * - 담당 잠재고객인지 권한 확인
     * - 부모 + 잠재고객 상세정보 조회
     *
     * @param potentialCustomerId 잠재고객 ID
     * @return 잠재고객 상세정보
     */
    @Operation(summary = "잠재고객 상세 조회", description = "부모 정보와 잠재고객 정보를 함께 조회합니다.")
    @GetMapping("/{potentialCustomerId}")
    public ApiResponse<PotentialCustomerDetailResponse> getPotentialCustomerDetail(
            @PathVariable Long potentialCustomerId
    ) {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        PotentialCustomerDetailResponse response =
                potentialCustomerService.getPotentialCustomerDetail(
                        potentialCustomerId,
                        salesUserId
                );

        return ApiResponse.success(
                200,
                "잠재고객 상세 조회에 성공했습니다.",
                response
        );
    }

    /**
     * 잠재고객 수정 API
     *
     * 기능:
     * - 잠재고객의 자녀 정보를 수정한다.
     * - 부모 정보는 통합고객 원장이므로 수정하지 않는다.
     *
     * 수정 대상:
     * - 이름
     * - 성별
     * - 생년월일
     *
     * @param potentialCustomerId 수정할 잠재고객 ID
     * @param request 수정 요청 정보
     * @return 수정된 잠재고객 상세 정보
     */
    @Operation(summary = "잠재고객 수정", description = "잠재고객의 자녀 정보를 수정합니다.")
    @PatchMapping("/{potentialCustomerId}")
    public ApiResponse<PotentialCustomerDetailResponse> updatePotentialCustomer(
            @PathVariable Long potentialCustomerId,
            @Valid @RequestBody PotentialCustomerUpdateRequest request
    ) {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        PotentialCustomerDetailResponse response =
                potentialCustomerService.updatePotentialCustomer(
                        potentialCustomerId,
                        request,
                        salesUserId
                );

        return ApiResponse.success(
                200,
                "잠재고객 수정에 성공했습니다.",
                response
        );
    }

    /**
     * 부모 통합고객 목록 조회 API
     *
     * 기능:
     * - 로그인한 영업사원이 담당하는 부모 통합고객 목록을 조회한다.
     *
     * 처리 흐름:
     * - SecurityUtil에서 현재 로그인한 영업사원 ID를 가져온다.
     * - Service에서 부모 통합고객 목록을 조회한다.
     * - 조회 결과를 반환한다.
     *
     * @return 담당 부모 통합고객 목록
     */
    @Operation(summary = "부모 통합고객 목록 조회", description = "로그인한 영업사원이 담당하는 부모 통합고객 목록을 조회합니다.")
    @GetMapping("/parents")
    public ApiResponse<List<ParentCustomerSearchResponse>> getParentCustomers() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        List<ParentCustomerSearchResponse> response =
                potentialCustomerService.getParentCustomers(salesUserId);

        return ApiResponse.success(
                200,
                "부모 통합고객 목록 조회에 성공했습니다.",
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
     * - rrn은 Service에서 해시 처리 후 DB의 rrn_encrypted와 비교한다.
     *
     * @param request 부모 통합고객 조회 요청 정보
     * @return 부모 통합고객 정보
     *
    @Operation(summary = "부모 통합고객 조회", description = "잠재고객 등록 전 부모 통합고객 존재 여부를 조회하는 API입니다.")
    @PostMapping("/parent/search")
    public ApiResponse<ParentCustomerSearchResponse> findParentCustomer(
            @Valid @RequestBody ParentCustomerSearchRequest request
    ) {
        Long salesUserId = SecurityUtil.getCurrentUserId();

        ParentCustomerSearchResponse response =
                potentialCustomerService.findParentCustomer(request, salesUserId);

        return ApiResponse.success(
                200,
                "부모 통합고객 정보 조회에 성공했습니다.",
                response
        );
    }
     */

    /**
     * 잠재고객 등록 API
     *
     * 기능:
     * - 부모 통합고객과 연결된 자녀 잠재고객을 등록한다.
     *
     * 처리 흐름:
     * - 현재 로그인한 영업사원 ID를 SecurityUtil에서 가져온다.
     * - 부모 통합고객 존재 여부 확인
     * - 중복 잠재고객 여부 확인
     * - 잠재고객 정보 저장
     * - 등록 완료된 잠재고객 정보 반환
     *
     * 현재는 SecurityUtil에서 테스트용 사용자 ID를 가져온다.
     * 추후 JWT 적용 시 SecurityUtil 내부 로직만 실제 로그인 사용자 추출 방식으로 변경하면 된다.
     *
     * @param request 잠재고객 등록 요청 정보
     * @return 등록 완료된 잠재고객 정보
     */
    @Operation(summary = "잠재고객 등록", description = "부모 통합고객과 연결된 자녀 잠재고객을 등록하는 API입니다.")
    @PostMapping
    public ApiResponse<PotentialCustomerCreateResponse> createPotentialCustomer(
            @Valid @RequestBody PotentialCustomerCreateRequest request
    ) {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        PotentialCustomerCreateResponse response =
                potentialCustomerService.createPotentialCustomer(
                        request,
                        salesUserId
                );

        return ApiResponse.success(
                201,
                "잠재고객 등록에 성공했습니다.",
                response
        );
    }

    /**
     * 잠재고객 삭제 API
     *
     * 기능:
     * - 잠재고객 정보를 삭제한다.
     * - 삭제 전 라이프사이클 로그를 저장한다.
     *
     * 처리 흐름:
     * - 현재 로그인한 영업사원 ID를 SecurityUtil에서 가져온다.
     * - 삭제 대상 존재 여부 확인
     * - 담당 영업사원 권한 확인
     * - lifecycle_log 저장
     * - 잠재고객 삭제
     *
     * 현재는 salesUserId에서 테스트용 사용자 ID를 가져온다.
     * 추후 JWT 적용 시 SecurityUtil 내부 로직만 실제 로그인 사용자 추출 방식으로 변경하면 된다.
     *
     * @param potentialCustomerId 삭제할 잠재고객 ID
     * @return 삭제 결과
     */
    @Operation(summary = "잠재고객 삭제", description = "잠재고객을 삭제하고 라이프사이클 로그를 저장하는 API입니다.")
    @DeleteMapping("/{potentialCustomerId}")
    public ApiResponse<PotentialCustomerDeleteResponse> deletePotentialCustomer(
            @PathVariable Long potentialCustomerId
    ) {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        PotentialCustomerDeleteResponse response =
                potentialCustomerService.deletePotentialCustomer(
                        potentialCustomerId,
                        salesUserId
                );

        return ApiResponse.success(
                200,
                "잠재고객 삭제에 성공했습니다.",
                response
        );
    }
}
