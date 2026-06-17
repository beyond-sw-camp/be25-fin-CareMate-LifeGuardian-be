package com.caremate.lifeguardian.common.redis;

public final class RedisKeyConstants {

	/**
	 * 객체 생성을 막기 위한 private 생성자
	 *
	 * 이 클래스는 상수만 모아둔 클래스이므로 new RedisKeyConstants()로 생성할 필요가 없다.
	 */
	private RedisKeyConstants() {
	}

	// Refresh Token 저장용 Prefix
	public static final String REFRESH_TOKEN_PREFIX = "RT:";

	// Access Token 블랙리스트 저장용 Prefix
	public static final String BLACKLIST_PREFIX = "BL:";


	public static final String RECOMMENDATION_PREFIX = "RECOMMENDATION";

}