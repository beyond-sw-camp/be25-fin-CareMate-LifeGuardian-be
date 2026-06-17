package com.caremate.lifeguardian.auth.controller;

import com.caremate.lifeguardian.auth.dto.request.InitialPasswordResetRequest;
import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.AuthResultDto;
import com.caremate.lifeguardian.auth.dto.response.LoginResponse;
import com.caremate.lifeguardian.auth.service.AuthService;
import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.CookieUtil;
import com.caremate.lifeguardian.common.security.JwtProvider;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그인 API")
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
	@Operation(summary = "로그인")
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

	@Operation(summary = "최초 로그인 비밀번호 변경")
	@PatchMapping("/initial-password")
	public ResponseEntity<ApiResponse<Void>> resetInitialPassword(
			@Valid @RequestBody InitialPasswordResetRequest request,
			HttpServletRequest httpServletRequest
	) {
		Long userId = SecurityUtil.getCurrentUserId();

		// 접속 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// 비밀번호 변경 처리
		authService.resetInitialPassword(userId, request, ipAddress, userAgent);

		return ResponseEntity.ok(
				ApiResponse.success(200, "최초 로그인 비밀번호 재설정이 완료되었습니다.", null)
		);
	}

	@Operation(summary = "Access Token 재발급")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<LoginResponse>> reissue(
			@CookieValue(value = CookieUtil.REFRESH_TOKEN_NAME, required = false) String refreshToken,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse
	) {
		// 접속 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// 토큰 재발급 처리
		AuthResultDto authResult = authService.reissue(refreshToken, ipAddress, userAgent);

		// 신규 Refresh Token 쿠키 저장
		cookieUtil.addRefreshTokenCookie(
				httpServletResponse,
				authResult.getRefreshToken(),
				jwtProvider.getRefreshTokenStepSeconds()
		);

		return ResponseEntity.ok(
				ApiResponse.success(200, "토큰이 재발급되었습니다.", authResult.toLoginResponse())
		);
	}
}