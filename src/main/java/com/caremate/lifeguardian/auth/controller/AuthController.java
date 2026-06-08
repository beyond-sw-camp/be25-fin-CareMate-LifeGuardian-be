package com.caremate.lifeguardian.auth.controller;

import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.response.LoginResponse;
import com.caremate.lifeguardian.auth.service.AuthService;
import com.caremate.lifeguardian.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpServletRequest
	) {
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		LoginResponse response = authService.login(request, ipAddress, userAgent);

		return ResponseEntity.ok()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAccessToken())
				.body(ApiResponse.success(200, "로그인에 성공했습니다.", response));
	}

	// 사용자의 IP 주소를 추출하기 위해
	private String getClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0];
		}

		return request.getRemoteAddr();
	}
}