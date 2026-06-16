package com.caremate.lifeguardian.auth.mapper;

import com.caremate.lifeguardian.member.domain.SalesUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuthMapper {

	SalesUser findByLoginId(@Param("loginId") String loginId);

	void blacklistPreviousTokens(@Param("userId") Long userId);

	void insertTokenManagement(
			@Param("userId") Long userId,
			@Param("refreshToken") String refreshToken,
			@Param("clientIp") String clientIp,
			@Param("userAgent") String userAgent,
			@Param("expiryAt") LocalDateTime expiryAt
	);

	void insertAuditLog(
			@Param("salesUserId") Long salesUserId,
			@Param("actionTypeCode") String actionTypeCode,
			@Param("ipAddress") String ipAddress,
			@Param("userAgent") String userAgent,
			@Param("reason") String reason
	);

	SalesUser findById(@Param("userId") Long userId);

	void updateInitialPassword(
			@Param("userId") Long userId,
			@Param("passwordHash") String passwordHash,
			@Param("termsAgreed") Boolean termsAgreed
	);
}