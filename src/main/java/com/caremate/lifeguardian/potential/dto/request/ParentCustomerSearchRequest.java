package com.caremate.lifeguardian.potential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ParentCustomerSearchRequest {

    /**
     * 부모 통합고객 이름
     * integrated_customer.name과 비교
     */
    @NotBlank(message = "부모 이름은 필수 입니다.")
    private String name;

    /**
     * 부모 생년월일
     * integrated_customer.birth_date와 비교
     */
    @NotNull(message = "부모 생년월일은 필수입니다.")
    private LocalDate birthDate;

    /**
     * 부모와 자녀의 관계 코드
     *
     * RELATIONSHIP
     * 01 = 부
     * 02 = 모
     *
     * 화면에서는 부/모 선택값으로 입력받고,
     * service에서 gender로 변환해서 integrated_customer.gender와 비교
     *
     * 01(부) -> MALE
     * 02(모) -> FEMALE
     */
    @NotBlank(message = "부모와의 관계는 필수입니다.")
    private String relationshipCode;

    /**
     * 부모 연락처
     * integrated_customer.phone과 비교
     */
    @NotBlank(message = "부모 연락처는 필수입니다.")
    private String phone;

    /**
     * 부모 주민등록번호 식별값
     *
     * 입력 예:
     * 830411-1******
     *
     * 처리 방식:
     * - 하이픈(-) 제거
     * - * 제거
     * - 8304111 형태로 정규화
     * - SHA-256 해시 처리 후 ,integrated_customer.rrn_encrypted와 비교
     */
    @NotBlank(message = "주민등록번호는 필수입니다.")
    private String rrn;

    /**
     * 부모 주소
     * integrated_customer.address와 비교
     */
    @NotBlank(message = "주소는 필수입니다.")
    private String address;
}
