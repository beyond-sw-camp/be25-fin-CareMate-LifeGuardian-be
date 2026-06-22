package com.caremate.lifeguardian.webform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영업현황 웹폼 일괄 발송 대상 DTO
 *
 * 잠재고객과 통합고객을 함께 조회하기 위해 사용한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesStatusWebformTargetResponse {

    /**
     * 고객 Id
     */
    private Long customerId;

    /**
     * 고객 상태 구분 코드
     *
     * 01: 잠재고객
     * 02: 통합고객
     */
    private String conversionStatusCode;
}
