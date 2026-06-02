package com.caremate.lifeguardian.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUserRegisterRequest {

    @NotBlank(message = "영업사원 성명은 필수 입력 항목입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수 입력 항목입니다.")
    private LocalDate birthDate;

    @NotNull(message = "소속 지점 ID는 필수 입력 항목입니다.")
    private Long branchId;

    @NotBlank(message = "직급 코드는 필수 입력 항목입니다.")
    private String rankCode;

    @NotBlank(message = "업무용 휴대폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(010-XXXX-XXXX)이 아닙니다.")
    private String phone;

    @NotBlank(message = "업무용 이메일 주소는 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "입사일은 필수 입력 항목입니다.")
    private LocalDate joinedAt;

    private String roleCode; // 미입력 시 서비스 단에서 '02'(일반 영업사원)로 할당
}
