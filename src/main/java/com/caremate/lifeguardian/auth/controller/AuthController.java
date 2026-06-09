package com.caremate.lifeguardian.auth.controller;

import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.response.AuthResultDto;
import com.caremate.lifeguardian.auth.dto.response.LoginResponse;
import com.caremate.lifeguardian.auth.service.AuthService;
import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.CookieUtil;
import com.caremate.lifeguardian.common.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
	private final CookieUtil cookieUtil;
	private final JwtProvider jwtProvider;

	/**
	 * 사용자 일반 로그인 API (쿠키 모듈화 및 토큰 반환 방식 변경 적용)
	 */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse
	) {
		// IP 및 기기 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// 로그인 비즈니스 로직 수행 및 토큰 세션 생성
		AuthResultDto authResult = authService.login(request, ipAddress, userAgent);

		// HttpOnly Refresh Token 쿠키 생성
		ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
				authResult.getRefreshToken(),
				jwtProvider.getRefreshTokenStepSeconds()
		);

		// 응답 헤더에 쿠키 추가
		httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		LoginResponse loginResponse = authResult.toLoginResponse();

		return ResponseEntity.ok(ApiResponse.success(200, "로그인에 성공했습니다.", loginResponse));
	}

	// 사용자의 IP 주소를 추출하기 위해
	private String getClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}
}