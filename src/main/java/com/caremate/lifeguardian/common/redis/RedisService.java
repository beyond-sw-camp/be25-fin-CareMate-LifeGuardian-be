package com.caremate.lifeguardian.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

	private final RedisTemplate<String, String> redisTemplate;

	// Redis에 문자열 값을 저장한다.
	public void save(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	// Redis에 문자열 값을 만료시간과 함께 저장한다.
	public void save(String key, String value, long expirationSeconds) {
		redisTemplate.opsForValue()
				.set(key, value, Duration.ofSeconds(expirationSeconds));
	}

	// Redis에서 Key에 해당하는 값을 조회한다.
	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	// Redis에서 Key에 해당하는 데이터를 삭제한다.
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	// Redis에 해당 Key가 존재하는지 확인한다.
	public boolean exists(String key) {
		Boolean result = redisTemplate.hasKey(key);
		return Boolean.TRUE.equals(result);
	}

	// Redis Key의 남은 만료시간을 조회한다.
	public Long getExpire(String key) {
		return redisTemplate.getExpire(key);
	}
}