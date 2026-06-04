package com.caremate.lifeguardian.potential.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ParentCustomerSearchResponse {

    /**
     * 부모 통합고객 ID
     *
     * 잠재고객 등록 시 parentCustomerId로 사용된다.
     */
    private Long integratedCustomerId;

    /**
     * 부모 이름
     */
    private String name;

    /**
     * 부모 생년월일
     */
    private LocalDate birthDate;

    /**
     * 부모와 자녀의 관계 코드
     *
     * RELATIONSHIP
     * 01 = 부
     * 02 = 모
     */
    private String relationshipCode;

    /**
     * 관계명
     *
     * 예: 부/모
     */
    private String relationshipName;

    /**
     * 부모 연락처
     */
    private String phone;

    /**
     * 부모 주소
     */
    private String address;
}
