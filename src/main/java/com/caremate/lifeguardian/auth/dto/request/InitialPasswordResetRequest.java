package com.caremate.lifeguardian.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InitialPasswordResetRequest {

	@NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
	@Pattern(
			regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$",
			message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
	)
	private String newPassword;

	@NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
	private String confirmPassword;

	@AssertTrue(message = "개인정보 처리방침 및 보안 준수 서약에 동의해야 합니다.")
	private Boolean privacyPolicyAgreed;
}