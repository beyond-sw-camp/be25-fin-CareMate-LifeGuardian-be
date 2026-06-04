package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import java.util.List;

public interface PotentialCustomerService {

    /* 잠재고객 목록 조회
    - @param salesUSerId 로그인한 영업사원 ID
    - @return 잠재고객 목록
     */
    List<PotentialCustomerListResponse> getPotentialCustomers(Long salesUSerId);

    /**
     * 부모 통합고개 조회
     *
     * 입력한 부모 정보와 일치하는 통합고객 조회
     *
     * @param request 부모 조회 요청 정보
     * @return 부모 통합고객 정보
     */
    ParentCustomerSearchResponse findParentCustomer(
            ParentCustomerSearchRequest request
    );
}
