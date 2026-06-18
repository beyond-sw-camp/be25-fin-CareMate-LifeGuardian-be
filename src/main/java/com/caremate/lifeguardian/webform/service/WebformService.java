package com.caremate.lifeguardian.webform.service;

import com.caremate.lifeguardian.webform.dto.response.WebformSendResponse;

import java.util.List;

public interface WebformService {

    /**
     * 웹폼 개별 발송
     *
     * 처리 내용
     * - 현재 로그인한 영업사원 ID를 기준으로 웹폼 발송 이력을 생성한다.
     * - UUID 토큰을 생성한다.
     * - 웹폼 상태를 발송완료(02)로 저장한다.
     *
     * @param customerId 웹폼을 발송할 고객 ID
     * @return 웹폼 발송 결과
     */
    WebformSendResponse sendWebform(Long customerId);

    /**
     * 웹폼 일괄 발송
     *
     * 처리 내용:
     * - 로그인한 영업사원의 오늘 웹폼 발송 대상 고객을 조회한다.
     * - 오늘 생일인 잠재고객 중 아직 발송완료되지 않은 고객에게 발송한다.
     *
     * @return 웹폼 발송 결과 목록
     */
    List<WebformSendResponse> sendBulkWebform();

    /**
     * 웹폼 회수 처리
     *
     * 처리 내용:
     * - UUID 토큰으로 웹폼 발송 이력을 찾는다.
     * - 웹폼 상태를 회수/만료(04)로 변경한다.
     * - 회수일시를 저장한다.
     * - 잠재고객 상담 상태를 상담중(02)으로 변경한다.
     * - 잠재고객 웹폼 회수일을 저장한다.
     *
     * @param uuidToken 웹폼 UUID 토큰
     */
    void collectWebform(String uuidToken);
}
