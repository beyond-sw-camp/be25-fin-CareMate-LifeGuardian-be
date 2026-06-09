package com.caremate.lifeguardian.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResultDto {
	private String accessToken;
	private String refreshToken; // 컨트롤러가 쿠키를 구울 때만 참조하고 버려짐
	private Long userId;
	private String name;
	private String role;
	private Boolean isFirstLogin;

	// 바디 전송용 LoginResponse로 변환해주는 매퍼 메서드
	public LoginResponse toLoginResponse() {
		return LoginResponse.builder()
				.accessToken(this.accessToken)
				.userId(this.userId)
				.name(this.name)
				.role(this.role)
				.isFirstLogin(this.isFirstLogin)
				.build();
	}
}