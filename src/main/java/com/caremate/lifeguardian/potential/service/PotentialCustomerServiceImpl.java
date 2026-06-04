package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import com.caremate.lifeguardian.potential.mapper.PotentialCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PotentialCustomerServiceImpl implements PotentialCustomerService {

    private final PotentialCustomerMapper potentialCustomerMapper;

    /* 잠재고객 목록 조회 실제 구현
    처리 흐름:
    - Controller에서 로그인한 영업사원 ID를 전달받는다.
    - Mapper를 호출해서 DB에서 잠재고객 목록을 조회한다.
    - 조회된 목록을 controller로 반환한다.
     */

    @Override
    @Transactional(readOnly = true)
    public List<PotentialCustomerListResponse> getPotentialCustomers(Long salesUserId) {
        return potentialCustomerMapper.findPotentialCustomersBySalesUserId(salesUserId);
    }
}
