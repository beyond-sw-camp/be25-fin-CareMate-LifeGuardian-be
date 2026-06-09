package com.caremate.lifeguardian.common.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
	public static final String REFRESH_TOKEN_NAME = "refreshToken";

	/**
	 * 리프레시 토큰 전용 보안 쿠키 생성
	 * @param refreshToken       발급된 JWT Refresh Token
	 * @param maxAgeSeconds      만료 시간 (초) -> jwtProvider.getRefreshTokenStepSeconds() 매핑용
	 */
	public ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
		return ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken) // 상수로 변경하여 일관성 유지
				.httpOnly(true)   // XSS 공격 방지
				.secure(false)    // 빌드/로컬 테스트 환경 안정성을 위해 일단 false 처리 (운영 환경 적용시 상단이나 yml 설정 연동 권장)
				.path("/")        // 전체 경로 전송 허용
				.maxAge(maxAgeSeconds)
				.sameSite("Lax")  // CSRF 방지 정책
				.build();
	}

	/**
	 * 쿠키 완전히 삭제 (로그아웃 혹은 토큰 만료 탈퇴 시 사용)
	 * 브라우저에서 쿠키가 확실히 삭제되려면 생성 시점과 설정(Path, HttpOnly, SameSite)이 일치해야 합니다.
	 */
	public ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_NAME, "")
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/")
				.maxAge(0) // 만료 시간을 0으로 주어 즉시 브라우저에서 소멸하게 만듦
				.build();
	}
}