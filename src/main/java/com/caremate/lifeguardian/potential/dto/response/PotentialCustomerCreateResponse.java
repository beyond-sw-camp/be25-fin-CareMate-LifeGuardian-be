package com.caremate.lifeguardian.potential.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PotentialCustomerCreateResponse {

    /**
     * 생성된 잠재고객 ID
     */
    private Long potentialCustomerId;

    /**
     * 부모 통합고객 ID
     */
    private Long parentCustomerId;

    /**
     * 부모와의 관계 코드
     * 01 = 부
     * 02 = 모
     */
    private String relationshipCode;

    /**
     * 관계명
     * 예: 부 / 모
     */
    private String relationshipName;

    /**
     * 자녀 이름
     */
    private String name;

    /**
     * 자녀 성별
     */
    private String gender;

    /**
     * 자녀 생년월일
     */
    private LocalDate birthDate;

    /**
     * 상담 상태 코드
     * 01 = 미상담
     */
    private String consultStatusCode;

    /**
     * 상담 상태명
     */
    private String consultStatusName;

    /**
     * 고객 전환 상태 코드
     * 01 = 잠재고객
     */
    private String conversionStatusCode;

    /**
     * 고객 전환 상태명
     */
    private String conversionStatusName;

    /**
     * 잠재고객 등록일시
     */
    private LocalDateTime createdAt;

}
