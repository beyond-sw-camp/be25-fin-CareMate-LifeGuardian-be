package com.caremate.lifeguardian.potential.mapper;

import com.caremate.lifeguardian.potential.domain.PotentialCustomer;
import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerCreateResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PotentialCustomerMapper {

    // 잠재고객 목록 조회
    List<PotentialCustomerListResponse> findPotentialCustomersBySalesUserId(
            @Param("salesUserId") Long salesUserId
    );

    // 부모 통합고객 조회
    ParentCustomerSearchResponse findParentCustomer(
            @Param("request") ParentCustomerSearchRequest request,
            @Param("gender") String gender,
            @Param("hashedRrn") String hashedRrn,
            @Param("salesUserId") Long salesUserId
    );

    // 부모 통합고객 존재 여부 확인(잠재고객 등록 전 parentCustomerId가 실제 존재하는지 검증)
    boolean existsParentCustomer(
            @Param("parentCustomerId") Long parentCustomerId
    );

    // 중복 잠재고객 존재 여부 확인
    boolean existsDuplicatePotentialCustomer(
            @Param("parentCustomerId") Long parentCustomerId,
            @Param("name") String name,
            @Param("gender") String gender,
            @Param("birthDate") LocalDate birthDate
    );

    // 잠재고객 등록
    int insertPotentialCustomer(
            PotentialCustomer potentialCustomer
    );

    // 등록 완료된 잠재고객 단건 조회(등록 성공 후 응답 DTO 생성을 위해 사용)
    PotentialCustomerCreateResponse findCreatedPotentialCustomer(
            @Param("potentialCustomerId") Long potentialCustomerId
    );

    // 삭제 대상 잠재고객 조회
    PotentialCustomer findPotentialCustomerById(
            @Param("potentialCustomerId") Long potentialCustomerId
    );

    // 삭제 권한 확인
    boolean existsPotentialCustomerByIdAndSalesUserId(
            @Param("potentialCustomerId") Long potentialCustomerId,
            @Param("salesUserId") Long salesUserId
    );

    // 잠재고객 라이프사이클 로그 저장
    int insertPotentialCustomerLifecycleLog(
            @Param("potentialCustomer") PotentialCustomer potentialCustomer,
            @Param("actionTypeCode") String actionTypeCode,
            @Param("snapshotData") String snapshotData
    );

    // 잠재고객 삭제
    int deletePotentialCustomer(
            @Param("potentialCustomerId") Long potentialCustomerId
    );
}
