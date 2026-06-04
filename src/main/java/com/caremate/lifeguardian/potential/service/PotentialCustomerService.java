package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import java.util.List;

public interface PotentialCustomerService {

    /* 잠재고객 목록 조회
    - @param salesUSerId 로그인한 영업사원 ID
    - @return 잠재고객 목록
     */

    List<PotentialCustomerListResponse> getPotentialCustomers(Long salesUSerId);
}
