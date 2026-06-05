package com.caremate.lifeguardian.potential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PotentialCustomerCreateRequest {

    /**
     * 부모 통합 고객 ID
     *
     * 부모 조회 API 성공 후 전달받은 integratedCustomerId 값을 사용한다.
     */
    @NotNull(message = "부모 통합고객 ID는 필수입니다.")
    private Long parentCustomerId;

    /**
     * 부모와의 관계 코드
     *
     * RELATIONSHIP
     * 01 = 부
     * 02 = 모
     */
    @NotBlank(message = "부모와의 관계는 필수입니다.")
    private String relationshipCode;

    /**
     * 자녀 이름
     */
    @NotBlank(message = "자녀 이름은 필수입니다.")
    private String name;

    /**
     * 성별
     *
     * MALE / FEMALE 형태로 저장
     */
    @NotBlank(message = "자녀 성별은 필수입니다.")
    private String gender;

    /**
     * 자녀 생년월일
     */
    @NotNull(message = "자녀 생년월일은 필수입니다.")
    private LocalDate birthDate;
}
