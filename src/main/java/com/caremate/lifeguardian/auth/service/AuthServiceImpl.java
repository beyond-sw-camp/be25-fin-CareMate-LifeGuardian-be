package com.caremate.lifeguardian.auth.service;

import com.caremate.lifeguardian.auth.dto.request.InitialPasswordResetRequest;
import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.AuthResultDto;
import com.caremate.lifeguardian.auth.mapper.AuthMapper;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.redis.RedisKeyGenerator;
import com.caremate.lifeguardian.common.security.JwtProvider;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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
	public AuthResultDto login(
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
		return AuthResultDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.userId(user.getId())
				.name(user.getName())
				.branchId(user.getBranchId())
				.branchName(user.getBranchName())
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

	@Override
	@Transactional
	public void resetInitialPassword(
			Long userId,
			InitialPasswordResetRequest request,
			String ipAddress,
			String userAgent
	) {
		SalesUser user = authMapper.findById(userId);

		if (user == null) {
			throw new BaseException(404, "사용자 정보를 찾을 수 없습니다.");
		}

		if (!ACTIVE_STATUS_CODE.equals(user.getStatusCode())) {
			throw new BaseException(403, "비활성화된 계정입니다. 관리자에게 문의하세요.");
		}

		if (!Boolean.TRUE.equals(user.getIsTempPassword())) {
			throw new BaseException(403, "이미 최초 로그인 설정이 완료된 사용자입니다.");
		}

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new BaseException(400, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
		}

		if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
			throw new BaseException(400, "임시 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
		}

		// 새 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.getNewPassword());

		// 비밀번호 변경 및 최초 로그인 상태 해제
		authMapper.updateInitialPassword(
				userId,
				encodedPassword,
				request.getPrivacyPolicyAgreed()
		);

		// 감사 로그 저장
		authMapper.insertAuditLog(
				userId,
				"07",
				ipAddress,
				userAgent,
				"최초 로그인 비밀번호 재설정"
		);
	}

	@Override
	@Transactional
	public AuthResultDto reissue(
			String refreshToken,
			String ipAddress,
			String userAgent
	) {

		// Refresh Token 존재 여부 확인
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BaseException(401, "Refresh Token이 존재하지 않습니다. 다시 로그인해주세요.");
		}

		// 토큰에서 사용자 ID 추출
		Long userId = jwtProvider.getMemberId(refreshToken);

		// Redis 저장 토큰 조회
		String redisKey = RedisKeyGenerator.refreshToken(userId);
		String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

		// 로그인 세션 존재 여부 확인
		if (savedRefreshToken == null) {
			throw new BaseException(401, "로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
		}

		// Refresh Token 위변조 및 탈취 여부 확인
		if (!savedRefreshToken.equals(refreshToken)) {
			redisTemplate.delete(redisKey);
			authMapper.blacklistPreviousTokens(userId);

			throw new BaseException(401, "유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
		}

		// 사용자 조회
		SalesUser user = authMapper.findById(userId);

		if (user == null) {
			throw new BaseException(404, "사용자 정보를 찾을 수 없습니다.");
		}

		// 계정 활성 상태 확인
		if (!ACTIVE_STATUS_CODE.equals(user.getStatusCode())) {
			redisTemplate.delete(redisKey);
			authMapper.blacklistPreviousTokens(userId);

			throw new BaseException(403, "비활성화된 계정입니다. 관리자에게 문의하세요.");
		}

		// 권한 정보 변환
		Role role = convertRole(user.getRoleCode());

		// 신규 Access / Refresh Token 발급
		String newAccessToken = jwtProvider.createAccessToken(user.getId(), role);
		String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

		// Redis Refresh Token 갱신
		redisTemplate.delete(redisKey);
		redisTemplate.opsForValue().set(
				redisKey,
				newRefreshToken,
				Duration.ofSeconds(jwtProvider.getRefreshTokenStepSeconds())
		);

		// 기존 Refresh Token 무효화
		authMapper.blacklistPreviousTokens(user.getId());

		// 신규 Refresh Token 이력 저장
		authMapper.insertTokenManagement(
				user.getId(),
				newRefreshToken,
				ipAddress,
				userAgent,
				jwtProvider.getRefreshTokenExpire()
		);

		// 재발급 응답 반환
		return AuthResultDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.userId(user.getId())
				.name(user.getName())
				.role(role.name())
				.isFirstLogin(user.getIsTempPassword())
				.build();
	}

	@Override
	@Transactional
	public void logout(
			Long userId,
			String ipAddress,
			String userAgent
	) {
		// Redis Refresh Token 삭제
		String redisKey = RedisKeyGenerator.refreshToken(userId);
		redisTemplate.delete(redisKey);

		// DB Refresh Token 이력 무효화 처리
		authMapper.blacklistPreviousTokens(userId);

		// 로그아웃 감사 로그 저장
		authMapper.insertAuditLog(
				userId,
				"06", // AUDIT_ACTION: LOGOUT 코드값에 맞게 수정
				ipAddress,
				userAgent,
				"로그아웃"
		);

		// 현재 요청의 인증 정보 제거
		SecurityContextHolder.clearContext();
	}
}