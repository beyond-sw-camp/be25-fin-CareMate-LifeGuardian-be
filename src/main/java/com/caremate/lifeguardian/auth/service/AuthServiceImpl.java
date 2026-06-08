package com.caremate.lifeguardian.auth.service;

import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.response.LoginResponse;
import com.caremate.lifeguardian.auth.mapper.AuthMapper;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.redis.RedisKeyGenerator;
import com.caremate.lifeguardian.common.security.JwtProvider;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AuthMapper authMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RedisTemplate<String, String> redisTemplate;

	// 상태 및 권한 공통 코드 정의
	private static final String ACTIVE_STATUS_CODE = "01";
	private static final String ROLE_ADMIN_CODE = "01";
	private static final String ROLE_SALES_CODE = "02";

	// AUDIT_ACTION common_code
	private static final String AUDIT_LOGIN_SUCCESS = "01";
	private static final String AUDIT_LOGIN_FAIL = "04";
	private static final String AUDIT_LOGIN_BLOCKED = "05";

	@Override
	@Transactional
	public LoginResponse login(
			LoginRequest request,
			String ipAddress,
			String userAgent
	) {
		// 아이디로 사용자 조회
		SalesUser user = authMapper.findByLoginId(request.getLoginId());

		// 아이디 존재 여부 확인
		if (user == null) {
			authMapper.insertAuditLog(
					null,
					AUDIT_LOGIN_FAIL,
					ipAddress,
					userAgent,
					"존재하지 않는 아이디로 로그인 시도"
			);

			throw new BaseException(401, "아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		// 계정 활성화 상태 확인
		if (!ACTIVE_STATUS_CODE.equals(user.getStatusCode())) {
			authMapper.insertAuditLog(
					user.getId(),
					AUDIT_LOGIN_BLOCKED,
					ipAddress,
					userAgent,
					"비활성화 계정 로그인 시도"
			);

			throw new BaseException(403, "비활성화된 계정입니다. 관리자에게 문의하세요.");
		}

		// 비밀번호 일치 여부 확인 (암호화 매칭)
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			authMapper.insertAuditLog(
					user.getId(),
					AUDIT_LOGIN_FAIL,
					ipAddress,
					userAgent,
					"비밀번호 불일치"
			);

			throw new BaseException(401, "아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		// 사용자 권한 변환 (String -> Enum)
		Role role = convertRole(user.getRoleCode());

		// 신규 JWT 토큰(Access / Refresh) 생성
		String accessToken = jwtProvider.createAccessToken(user.getId(), role);
		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		// Redis에 최신 Refresh Token 저장 (중복 로그인 방지 및 세션 갱신)
		String redisKey = RedisKeyGenerator.refreshToken(user.getId());

		redisTemplate.delete(redisKey);
		redisTemplate.opsForValue().set(
				redisKey,
				refreshToken,
				Duration.ofSeconds(jwtProvider.getRefreshTokenStepSeconds())
		);

		// RDB 작업: 기존 발급된 이전 토큰들을 일괄 무효화(블랙리스트) 처리
		authMapper.blacklistPreviousTokens(user.getId());

		// RDB 작업: 새로운 로그인 세션 정보(이력)를 토큰 관리 테이블에 등록
		authMapper.insertTokenManagement(
				user.getId(),
				refreshToken,
				ipAddress,
				userAgent,
				jwtProvider.getRefreshTokenExpire()
		);

		// RDB 작업: 최종 로그인 성공 감사 로그 기록
		authMapper.insertAuditLog(
				user.getId(),
				AUDIT_LOGIN_SUCCESS,
				ipAddress,
				userAgent,
				"로그인 성공"
		);

		// 클라이언트에게 전달할 인증 응답 객체 반환 (최초 로그인 여부 포함)
		return LoginResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.userId(user.getId())
				.name(user.getName())
				.role(role.name())
				.isFirstLogin(user.getIsTempPassword())
				.build();
	}

	/**
	 * DB의 String 권한 코드를 시스템 내부에서 사용하는 Role Enum으로 변환하는 메서드
	 */
	private Role convertRole(String roleCode) {
		if (ROLE_ADMIN_CODE.equals(roleCode)) {
			return Role.ADMIN;
		}

		if (ROLE_SALES_CODE.equals(roleCode)) {
			return Role.SALES;
		}

		throw new BaseException(500, "알 수 없는 사용자 권한입니다.");
	}
}