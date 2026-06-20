package com.caremate.lifeguardian.webform.mapper;

import com.caremate.lifeguardian.webform.dto.response.SalesStatusWebformTargetResponse;
import com.caremate.lifeguardian.webform.dto.response.WebformIssuanceTargetResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WebformMapper {

    /**
     * 대시보드용 웹폼 일괄 발송 대상 고객 ID 목록 조회
     *
     * 조건:
     * - 로그인한 영업사원의 담당 고객
     * - 잠재고객
     * - 오늘 생일인 고객
     * - 오늘 아직 웹폼 발송완료 상태가 아닌 고객
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @return 웹폼 발송 대상 잠재고객 ID 목록
     */
    List<Long> findTodayWebformSendTargetCustomerIds(
            @Param("salesUserId") Long salesUserId
    );

    /**
     * 영업현황 웹폼 일괄 발송 대상 조회
     *
     * 잠재고객 + 통합고객을 함께 조회한다.
     */
    List<SalesStatusWebformTargetResponse> findSalesStatusWebformTargets(
            @Param("salesUserId") Long salesUserId
    );

    /**
     * 잠재고객 존재 여부 확인
     *
     * 조건:
     * - 로그인한 영업사원이 담당하는 잠재고객
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @param customerId 잠재고객 ID
     * @return 존재 여부
     */
    boolean existsPotentialCustomerByIdAndSalesUserId(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId
    );

    /**
     * 통합고객 존재 여부 확인
     *
     * 조건:
     * - 로그인한 영업사원이 담당하는 통합고객
     *
     * @param salesUserId 로그인한 영업사원 ID
     * @param customerId 통합고객 ID
     * @return 존재 여부
     */
    boolean existsIntegratedCustomerByIdAndSalesUserId(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId
    );

    /**
     * 대시보드 웹폼 발송 대상 여부 확인
     *
     * 조건:
     * - 잠재고객
     * - 졸업 전 고객
     * - 오늘 생일인 고객
     * @param salesUserId 로그인한 영업사원 ID
     * @param customerId 잠재고객 ID
     * @return 발송 대상 여부
     */
    boolean existsDashboardWebformTarget(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId
    );

    /**
     * 웹폼 발송 이력을 저장한다.
     *
     * @param salesUserId 담당 영업사원 ID
     * @param customerId 대상 고객 ID
     * @param conversionStatusCode 고객 상태 코드 / 01: 잠재고객, 02: 통합고객
     * @param uuidToken 웹폼 UUID 토큰
     */
    void insertWebformIssuance(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode,
            @Param("uuidToken") String uuidToken
    );

    /**
     * 오늘 이미 발송된 웹폼이 있는지 확인한다.
     * 대시보드에서는 재발송을 허용하지 않는다.
     */
    boolean existsTodaySentWebform(
            @Param("salesUserId") Long salesUserId,
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode
    );

    /**
     * UUID 토큰으로 웹폼 발송 대상 정보를 조회한다.
     *
     * 조회 데이터:
     * - customerId
     * - conversionStatusCode
     *
     * 필요한 이유:
     * - 01: 잠재고객이면 potential_customer 상담 상태를 변경해야 한다.
     * - 02: 통합고객이면 potential_customer를 수정하면 안 된다.
     *
     * @param uuidToken 웹폼 UUID 토큰
     * @return 웹폼 발송 대상 정보
     */
    WebformIssuanceTargetResponse findIssuanceTargetByUuidToken(
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
     * - webform_received_at = 현재 날짜
     *
     * @param customerId 고객 ID
     * @return 수정 건수
     */
    int updatePotentialCustomerConsultStatus(
            @Param("customerId") Long customerId
    );
}
