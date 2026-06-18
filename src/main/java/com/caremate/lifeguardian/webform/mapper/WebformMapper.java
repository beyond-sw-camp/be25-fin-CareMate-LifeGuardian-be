package com.caremate.lifeguardian.webform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WebformMapper {

    /**
     * 웹폼 일괄 발송 대상 고객 ID 목록 조회
     *
     * 조건:
     * - 로그인한 영업사원의 담당 고객
     * - 잠재고객
     * - 오늘 생일인 고객
     * - 오늘 아직 웹폼 발송완료 상태가 아닌 고객
     */
    List<Long> findTodayWebformSendTargetCustomerIds(
            @Param("salesUserId") Long salesUserId
    );

    /**
     * 웹폼 발송 이력을 저장한다.
     *
     * @param salesUserId 담당 영업사원 ID
     * @param customerId 대상 고객 ID
     * @param conversionStatusCode 고객 상태 코드
     * @param uuidToken 웹폼 UUID 토큰
     */
    void insertWebformIssuance(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode,
            @Param("uuidToken") String uuidToken
    );

    /**
     * UUID 토큰으로 대상 고객 ID를 조회한다.
     *
     * @param uuidToken 웹폼 UUID 토큰
     * @return 고객 ID
     */
    Long findCustomerIdByUuidToken(
            @Param("uuidToken") String uuidToken
    );

    /**
     * 웹폼 회수 처리한다.
     *
     * 처리 내용:
     * - 웹폼 상태를 회수/만료(04)로 변경
     * - 회수 일시를 저장
     *
     * @param uuidToken 웹폼 UUID 토큰
     * @return 수정 건수
     */
    int updateWebformCollected(
            @Param("uuidToken") String uuidToken
    );

    /**
     * 잠재고객 상담 상태를 상담중으로 변경한다.
     *
     * 처리 내용:
     * - consult_status_code = '02'
     * - webform_received_at = 현재 시각
     *
     * @param customerId 고객 ID
     * @return 수정 건수
     */
    int updatePotentialCustomerConsultStatus(
            @Param("customerId") Long customerId
    );
}
