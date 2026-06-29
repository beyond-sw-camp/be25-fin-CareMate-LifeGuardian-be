package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerCreateRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerUpdateRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerCreateResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerDeleteResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerDetailResponse;
import java.util.List;

public interface PotentialCustomerService {

    /* 잠재고객 목록 조회
    - @param salesUSerId 로그인한 영업사원 ID
    - @return 잠재고객 목록
     */
    List<PotentialCustomerListResponse> getPotentialCustomers(Long salesUSerId);

    /**
     * 잠재고객 상세 조회
     *
     * 기능:
     * - 잠재고객 ID를 기준으로 부모 정보와 잠재고객 정보를 함께 조회한다.
     * - 로그인한 영업사원의 담당 잠재고객만 조회할 수 있다.
     *
     * @param potentialCustomerId 잠재고객 ID
     * @param salesUserId 로그인한 영업사원 ID
     * @return 잠재고객 상세 정보
     */
    PotentialCustomerDetailResponse getPotentialCustomerDetail(
            Long potentialCustomerId,
            Long salesUserId
    );

    /**
     * 잠재고객 수정
     *
     * 기능:
     * - 잠재고객의 자녀 정보를 수정한다.
     * - 부모 정보는 수정하지 않는다.
     * - 로그인한 영업사원의 담당 잠재고객만 수정할 수 있다.
     *
     * @param potentialCustomerId 잠재고객 ID
     * @param request 수정 요청 정보
     * @param salesUserId 로그인한 영업사원 ID
     * @return 수정된 잠재고객 상세 정보
     */
    PotentialCustomerDetailResponse updatePotentialCustomer(
            Long potentialCustomerId,
            PotentialCustomerUpdateRequest request,
            Long salesUserId
    );

    /**
     * 부모 통합고객 목록 조회
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @return 담당 부모 통합고객 목록
     */
    List<ParentCustomerSearchResponse> getParentCustomers(Long salesUserId);

    /**
     * 부모 통합고개 조회
     *
     * 입력한 부모 정보와 일치하는 통합고객 조회
     *
     * @param request 부모 조회 요청 정보
     * @return 부모 통합고객 정보
     */
    ParentCustomerSearchResponse findParentCustomer(
            ParentCustomerSearchRequest request,
            Long salesUserId
    );

    /**
     * 잠재고객 등록
     *
     * 처리 흐름:
     * - 부모 통합고객 존재 여부 확인
     * - 잠재고객 정보 저장
     * - 저장 완료 잠재고객 정보 반환
     *
     * @param request 잠재고객 등록 요청 정보
     * @param salesUserId 로그인한 영업사원 ID
     * @return 등록 완료된 잠재고객 정보
     */
    PotentialCustomerCreateResponse createPotentialCustomer(
            PotentialCustomerCreateRequest request,
            Long salesUserId
    );

    /**
     * 잠재고객 삭제
     *
     * @param potentialCustomerId 삭제할 잠재고객 ID
     * @param salesUserId 로그인한 영업사원 ID
     * @return 삭제 결과
     */
    PotentialCustomerDeleteResponse deletePotentialCustomer(
            Long potentialCustomerId,
            Long salesUserId
    );
}
