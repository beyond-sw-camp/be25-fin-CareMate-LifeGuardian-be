package com.caremate.lifeguardian.auth.controller;

import com.caremate.lifeguardian.auth.dto.AuthResultDto;
import com.caremate.lifeguardian.auth.dto.request.InitialPasswordResetRequest;
import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
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
import org.springframework.web.bind.annotation.*;

@Tag(
		name = "인증 API",
		description = "로그인, 로그아웃, 토큰 재발급, 최초 로그인 비밀번호 변경 API"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CookieUtil cookieUtil;
	private final JwtProvider jwtProvider;

	@PostMapping("/login")
	@Operation(summary = "로그인")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse
	) {
		// 로그인 이력 저장 및 보안 감사 로그 기록을 위해 접속 IP와 기기 정보를 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// 사용자 인증, Access Token/Refresh Token 발급, Redis 저장, 감사 로그 저장 수행
		AuthResultDto authResult = authService.login(request, ipAddress, userAgent);

		// Refresh Token은 JavaScript에서 접근할 수 없도록 HttpOnly Cookie로 생성
		ResponseCookie refreshTokenCookie =
				cookieUtil.createRefreshTokenCookie(
						authResult.getRefreshToken(),
						jwtProvider.getRefreshTokenStepSeconds()
				);

		// 생성한 Refresh Token Cookie를 Set-Cookie 응답 헤더에 추가
		addSetCookieHeader(httpServletResponse, refreshTokenCookie);

		// 클라이언트 Body에는 Access Token과 사용자 기본 정보만 반환
		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"로그인에 성공했습니다.",
						authResult.toLoginResponse()
				)
		);
	}

	@PatchMapping("/initial-password")
	@Operation(summary = "최초 로그인 비밀번호 변경")
	public ResponseEntity<ApiResponse<Void>> resetInitialPassword(
			@Valid @RequestBody InitialPasswordResetRequest request,
			HttpServletRequest httpServletRequest
	) {
		// SecurityContextHolder에 저장된 현재 로그인 사용자 ID 조회
		Long userId = SecurityUtil.getCurrentUserId();

		// 비밀번호 변경 감사 로그 기록을 위한 접속 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// 최초 로그인 비밀번호 변경 및 약관 동의 저장
		authService.resetInitialPassword(userId, request, ipAddress, userAgent);

		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"최초 로그인 비밀번호 재설정이 완료되었습니다.",
						null
				)
		);
	}

	@PostMapping("/reissue")
	@Operation(summary = "Access Token 재발급")
	public ResponseEntity<ApiResponse<LoginResponse>> reissue(
			@CookieValue(value = CookieUtil.REFRESH_TOKEN_NAME, required = false) String refreshToken,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse
	) {
		// 토큰 재발급 이력 저장을 위한 접속 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// Refresh Token 검증 후 신규 Access Token / Refresh Token 발급
		AuthResultDto authResult = authService.reissue(
				refreshToken,
				ipAddress,
				userAgent
		);

		// 새 Refresh Token을 HttpOnly Cookie로 생성
		ResponseCookie refreshTokenCookie =
				cookieUtil.createRefreshTokenCookie(
						authResult.getRefreshToken(),
						jwtProvider.getRefreshTokenStepSeconds()
				);

		// 기존 Refresh Token Cookie를 새 값으로 교체
		addSetCookieHeader(httpServletResponse, refreshTokenCookie);

		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"토큰이 재발급되었습니다.",
						authResult.toLoginResponse()
				)
		);
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse
	) {
		// SecurityContextHolder에 저장된 현재 로그인 사용자 ID 조회
		Long userId = SecurityUtil.getCurrentUserId();

		// 로그아웃 감사 로그 기록을 위한 접속 정보 추출
		String ipAddress = getClientIp(httpServletRequest);
		String userAgent = httpServletRequest.getHeader("User-Agent");

		// Redis Refresh Token 삭제 및 로그아웃 이력 저장
		authService.logout(userId, ipAddress, userAgent);

		// 브라우저에 저장된 Refresh Token Cookie 삭제용 쿠키 생성
		ResponseCookie refreshTokenCookie =
				cookieUtil.deleteRefreshTokenCookie();

		// Set-Cookie 헤더를 통해 refreshToken 쿠키 만료 처리
		addSetCookieHeader(httpServletResponse, refreshTokenCookie);

		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"로그아웃이 완료되었습니다.",
						null
				)
		);
	}

	/**
	 * Set-Cookie 응답 헤더 추가
	 *
	 * CookieUtil은 ResponseCookie 생성만 담당하고,
	 * Controller는 HTTP 응답 헤더 설정을 담당한다.
	 */
	private void addSetCookieHeader(
			HttpServletResponse response,
			ResponseCookie cookie
	) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * 클라이언트 IP 주소 추출
	 *
	 * 프록시 또는 로드밸런서를 거친 요청은 X-Forwarded-For 헤더에
	 * 실제 클라이언트 IP가 들어올 수 있다.
	 *
	 * X-Forwarded-For 값이 여러 개인 경우 첫 번째 값이 원 클라이언트 IP이다.
	 */
	private String getClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}
}