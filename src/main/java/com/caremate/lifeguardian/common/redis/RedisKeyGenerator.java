package com.caremate.lifeguardian.common.redis;

public final class RedisKeyGenerator {

	// 객체 생성을 막기 위한 private 생성자
	private RedisKeyGenerator() {
	}

	// Refresh Token 저장 Key 생성
	public static String refreshToken(Long userId) {
		return RedisKeyConstants.REFRESH_TOKEN_PREFIX + ":" + userId;
	}

	// Access Token 블랙리스트 Key 생성
	public static String blacklist(String accessToken) {
		return RedisKeyConstants.BLACKLIST_PREFIX + ":" + accessToken;
	}

	public static String recommendation(Long customerId) { return RedisKeyConstants.RECOMMENDATION_PREFIX + ":" + customerId; }
}