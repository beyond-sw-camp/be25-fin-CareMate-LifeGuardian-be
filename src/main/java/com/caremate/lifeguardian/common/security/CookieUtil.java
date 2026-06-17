package com.caremate.lifeguardian.common.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

	/**
	 * Refresh Token Cookie 이름
	 *
	 * 로그인, 재발급, 로그아웃에서 동일한 쿠키명을 사용해야
	 * 브라우저가 같은 쿠키로 인식한다.
	 */
	public static final String REFRESH_TOKEN_NAME = "refreshToken";

	/**
	 * Refresh Token 저장용 HttpOnly Cookie 생성
	 *
	 * 로그인 또는 토큰 재발급 성공 시 호출된다.
	 *
	 * @param refreshToken 발급된 JWT Refresh Token
	 * @param maxAgeSeconds 쿠키 만료 시간(초)
	 * @return Set-Cookie 헤더에 넣을 ResponseCookie 객체
	 */
	public ResponseCookie createRefreshTokenCookie(
			String refreshToken,
			long maxAgeSeconds
	) {
		return ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
				// JavaScript에서 쿠키 접근 불가
				// XSS 공격으로 Refresh Token이 탈취되는 위험을 줄인다.
				.httpOnly(true)

				// HTTPS 환경에서만 쿠키를 전송할지 여부
				// 로컬 개발 환경에서는 HTTP 테스트를 위해 false
				// 운영 HTTPS 환경에서는 true 권장
				.secure(false)

				// 모든 API 경로에서 refreshToken 쿠키를 사용할 수 있도록 설정
				.path("/")

				// 쿠키 만료 시간 설정
				// Refresh Token 만료 시간과 동일하게 맞추는 것이 일반적이다.
				.maxAge(maxAgeSeconds)

				// CSRF 위험을 줄이기 위한 SameSite 정책
				// Lax는 일반적인 페이지 이동에는 쿠키를 허용하고,
				// 일부 외부 사이트 요청에는 쿠키 전송을 제한한다.
				.sameSite("Lax")

				.build();
	}

	/**
	 * Refresh Token 삭제용 Cookie 생성
	 *
	 * 로그아웃 시 호출된다.
	 *
	 * 쿠키는 서버가 직접 삭제할 수 없으므로,
	 * 같은 이름/경로의 쿠키를 maxAge(0)으로 다시 내려보내
	 * 브라우저가 즉시 만료 처리하도록 만든다.
	 *
	 * @return Refresh Token 삭제용 ResponseCookie 객체
	 */
	public ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_NAME, "")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(0)
				.sameSite("Lax")
				.build();
	}
}