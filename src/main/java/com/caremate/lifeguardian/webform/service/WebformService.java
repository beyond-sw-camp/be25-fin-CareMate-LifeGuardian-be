package com.caremate.lifeguardian.webform.service;

import com.caremate.lifeguardian.webform.dto.response.WebformSendResponse;

import java.util.List;

public interface WebformService {

    /**
     * 웹폼 개별 발송
     *
     * 처리 내용
     * - 선택한 고객 1명에게 웹폼을 발송한다.
     * - conversionStatusCode에 따라 잠재고객/통합고객을 구분한다.
     * - UUID 토큰을 생성한다.
     * - 웹폼 상태를 발송완료(02)로 저장한다.
     *
     * @param conversionStatusCode 고객 상태 구분 코드 / 01=잠재고객, 02=통합고객
     * @param customerId 웹폼을 발송할 고객 ID
     * @return 웹폼 발송 결과
     */
    WebformSendResponse sendWebform(
            String sendSource,
            String conversionStatusCode,
            Long customerId
    );

    /**
     * 대시보드용 웹폼 일괄 발송
     *
     * 처리 내용:
     * - 오늘 연락 고객 목록에서 웹폼 발송 대상인 잠재고객에게 일괄 발송한다.
     * - 오늘 생일인 잠재고객 중 아직 발송완료되지 않은 고객만 발송한다.
     * - 통합고객은 포함하지 않는다.
     *
     * @return 웹폼 발송 결과 목록
     */
    List<WebformSendResponse> sendBulkWebform();

    /**
     * 영업현황용 웹폼 일괄 발송
     *
     * 처리 내용:
     * - 영업현황 화면에 표시되는 잠재고객과 통합고객 모두에게 웹폼을 일괄 발송한다.
     * - 잠재고객은 conversionStatusCode = 01로 저장한다.
     * - 통합고객은 conversionStatusCode = 02로 저장한다.
     *
     * @return 웹폼 발송 결과 목록
     */
    List<WebformSendResponse> sendSalesStatusBulkWebform();

    /**
     * 웹폼 회수 처리
     *
     * 처리 내용:
     * - UUID 토큰으로 웹폼 발송 이력을 찾는다.
     * - 웹폼 상태를 회수/만료(04)로 변경한다.
     * - 회수일시를 저장한다.
     * - 잠재고객인 경우 상담 상태를 상담중(02)으로 변경한다.
     * - 잠재고객인 경우 웹폼 회수일을 저장한다.
     * - 통합고객인 경우 webform_issuance 회수 처리만 수행한다.
     *
     * @param uuidToken 웹폼 UUID 토큰
     */
    void collectWebform(String uuidToken);
}
