package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
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

    /** 잠재고객 목록 조회 실제 구현
     *
     * 처리 흐름:
     * - Controller에서 로그인한 영업사원 ID를 전달받는다.
     * - Mapper를 호출해서 DB에서 잠재고객 목록을 조회한다.
     * - 조회된 목록을 controller로 반환한다.
     */

    @Override
    @Transactional(readOnly = true)
    public List<PotentialCustomerListResponse> getPotentialCustomers(Long salesUserId) {
        return potentialCustomerMapper.findPotentialCustomersBySalesUserId(salesUserId);
    }

    /**
     * 부모 통합고객 조회 실제 구현
     *
     * 처리 흐름:
     * - 화면에서 입력받은 관계코드(01=부, 02=모)를 DB의 gender 값으로 변환한다.
     * - 변환된 gender와 입력받은 부모 정보를 기준으로 integrated_customer를 조회한다.
     * - 일치하는 부모 통합고객이 없으면 404 예외를 발생시킨다.
     * - 조회 성공 시 부모 통합고객 정보를 반환한다.
     */

    @Override
    @Transactional(readOnly = true)
    public ParentCustomerSearchResponse findParentCustomer(ParentCustomerSearchRequest request) {

        String gender = convertRelationshipCodeToGender(request.getRelationshipCode());

        ParentCustomerSearchResponse response =
                potentialCustomerMapper.findParentCustomer(request, gender);

        if (response == null) {
            throw new BaseException(404, "일치하는 부모 통합고객 정보를 찾을 수 없습니다.");
        }

        return response;
    }

    /**
     * 관계코드를 성별값으로 변환
     *
     * RELATIONSHIP
     * 01 = 부 -> MALE
     * 02 = 모 -> FEMALE
     */
    private String convertRelationshipCodeToGender(String relationshipCode) {
        return switch (relationshipCode) {
            case "01" -> "MALE";
            case "02" -> "FEMALE";
            default -> throw new BaseException(400, "부모와의 관계 코드가 올바르지 않습니다.");
        };
    }
}
