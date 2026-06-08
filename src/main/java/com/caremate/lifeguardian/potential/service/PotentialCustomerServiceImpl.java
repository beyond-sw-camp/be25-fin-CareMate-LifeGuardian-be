package com.caremate.lifeguardian.potential.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.security.RrnHashUtil;
import com.caremate.lifeguardian.potential.domain.PotentialCustomer;
import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.request.PotentialCustomerCreateRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerCreateResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerDeleteResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import com.caremate.lifeguardian.potential.mapper.PotentialCustomerMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PotentialCustomerServiceImpl implements PotentialCustomerService {

    private final PotentialCustomerMapper potentialCustomerMapper;
    private final ObjectMapper objectMapper;
    private final RrnHashUtil rrnHashUtil;

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
    public ParentCustomerSearchResponse findParentCustomer(ParentCustomerSearchRequest request, Long salesUserId) {

        String gender = convertRelationshipCodeToGender(request.getRelationshipCode());

        // 주민번호 입력값을 식별키로 정규화 후 해시 처리
        // 예: 830411-1****** 또는 8304111 -> 8304111 -> SHA-256 + pepper
        String hashedRrn = rrnHashUtil.hash(request.getRrn());

        ParentCustomerSearchResponse response =
                potentialCustomerMapper.findParentCustomer(request, gender, hashedRrn, salesUserId);

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

    /**
     * 잠재고객 등록 실제 구현
     *
     * 처리 흐름:
     * - 부모 통합고객 ID가 실제 존재하는지 확인한다.
     * - 요청값과 로그인 영업사원 ID를 기반으로 PotentialCustomer 객체를 만든다.
     * - 잠재고객을 등록한다.
     * - 등록된 잠재고객 ID로 다시 조회하여 응답 데이터를 반환한다.
     */
    @Override
    @Transactional
    public PotentialCustomerCreateResponse createPotentialCustomer(
            PotentialCustomerCreateRequest request,
            Long salesUserId
    ) {
        // 1. 부모 통합고객 존재 여부 확인
        boolean existsParent =
                potentialCustomerMapper.existsParentCustomer(request.getParentCustomerId());

        if (!existsParent) {
            throw new BaseException(404, "부모 통합고객 정보를 찾을 수 없습니다.");
        }

        // 2. 중복 잠재고객 등록 여부 확인
        boolean existsDuplicate =
                potentialCustomerMapper.existsDuplicatePotentialCustomer(
                        request.getParentCustomerId(),
                        request.getName(),
                        request.getGender(),
                        request.getBirthDate()
                );

        if (existsDuplicate) {
            throw new BaseException(409, "이미 등록된 잠재고객입니다.");
        }

        // 3. 잠재고객 등록용 domain 객체 생성
        PotentialCustomer potentialCustomer = new PotentialCustomer();
        potentialCustomer.setParentCustomerId(request.getParentCustomerId());
        potentialCustomer.setSalesUserId(salesUserId);
        potentialCustomer.setRelationshipCode(request.getRelationshipCode());
        potentialCustomer.setName(request.getName());
        potentialCustomer.setGender(request.getGender());
        potentialCustomer.setBirthDate(request.getBirthDate());

        // 4.잠재고객 등록
        int insertedCount =
                potentialCustomerMapper.insertPotentialCustomer(potentialCustomer);

        if (insertedCount != 1) {
             throw new BaseException(500, "시스템 오류로 인해 잠재고객을 등록하지 못했습니다. 관리자에게 문의하세요.");
        }

        // 5. 등록 완료된 잠재고객 단건 조회 후 반환
        return potentialCustomerMapper.findCreatedPotentialCustomer(
                potentialCustomer.getId()
        );
    }

    /**
     * 잠재고객 삭제 실제 구현
     *
     * 처리 흐름:
     * - 삭제할 잠재고객이 존재하는지 확인한다.
     * - 로그인한 영업사원의 담당 잠재고객인지 확인한다.
     * - 삭제 전 잠재고객 정보를 JSON 스냅샷으로 생성한다.
     * - potential_customer_lifecycle_log에 삭제 이력을 저장한다.
     * - 잠재고객을 삭제한다.
     * - 삭제된 잠재고객 ID와 삭제 시간을 반환한다.
     */
    @Override
    @Transactional
    public PotentialCustomerDeleteResponse deletePotentialCustomer(
            Long potentialCustomerId,
            Long salesUserId
    ) {
        // 1. 삭제 대상 잠재고객 존재 여부 확인
        PotentialCustomer potentialCustomer =
                potentialCustomerMapper.findPotentialCustomerById(potentialCustomerId);

        if (potentialCustomer == null) {
            throw new BaseException(404, "해당 잠재고객 정보를 찾을 수 없습니다.");
        }

        // 2. 로그인한 영업사원의 담당 고객인지 확인
        boolean hasPermission =
                potentialCustomerMapper.existsPotentialCustomerByIdAndSalesUserId(
                        potentialCustomerId,
                        salesUserId
                );

        if (!hasPermission) {
            throw new BaseException(403, "해당 잠재고객을 삭제할 권한이 없습니다.");
        }

        // 3. 삭제 전 잠재고객 정보를 JSON 스냅샷으로 변환
        String snapshotData;

        try {
            snapshotData = objectMapper.writeValueAsString(potentialCustomer);
        } catch (JsonProcessingException e) {
            throw new BaseException(500, "시스템 오류로 인해 잠재고객 삭제 로그 생성에 실패했습니다. 관리자에게 문의하세요.");
        }

        // 4. 라이프사이클 로그 저장
        int logInsertedCount =
                potentialCustomerMapper.insertPotentialCustomerLifecycleLog(
                        potentialCustomer,
                        "03",  // PC_ACTION: 03 = 단순삭제
                        snapshotData
                );

        if (logInsertedCount != 1) {
            throw new BaseException(500, "시스템 오류로 인해 잠재고객 삭제 로그 저장에 실패했습니다. 관리자에게 문의하세요.");
        }

        // 5. 잠재고객 삭제
        int deletedCount =
                potentialCustomerMapper.deletePotentialCustomer(potentialCustomerId);

        if (deletedCount != 1) {
            throw new BaseException(500, "시스템 오류로 인해 잠재고객 삭제에 실패했습니다. 관리자에게 문의하세요.");
        }

        // 6. 삭제 결과 반환
        return PotentialCustomerDeleteResponse.builder()
                .potentialCustomerId(potentialCustomerId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
