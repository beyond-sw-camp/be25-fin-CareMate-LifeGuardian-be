package com.caremate.lifeguardian.webform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 웹폼 발행 대상 조회 응답 DTO
 *
 * uuidToken으로 웹폼 발행 이력을 조회했을 때
 * 회수 처리에 필요한 고객 ID와 고객 유형을 담는다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebformIssuanceTargetResponse {

    private Long salesUserId;

    /**
     * 웹폼 발송 대상 고객 ID
     *
     * conversionStatusCode에 따라
     * - 01: potential_customer.id
     * - 02: integrated_customer.integrated_customer_id
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
