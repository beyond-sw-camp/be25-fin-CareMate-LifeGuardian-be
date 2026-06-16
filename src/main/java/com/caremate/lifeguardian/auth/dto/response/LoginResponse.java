package com.caremate.lifeguardian.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

	private String accessToken;
	private Long userId;
	private String name;
	private Long branchId;
	private String branchName;
	private String role;
	private Boolean isFirstLogin;
}