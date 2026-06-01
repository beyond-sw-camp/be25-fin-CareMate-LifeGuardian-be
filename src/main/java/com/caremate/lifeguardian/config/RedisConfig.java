package com.caremate.lifeguardian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, String> redisTemplate(
			RedisConnectionFactory connectionFactory
	) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

		// Spring Boot가 application.yml 정보를 바탕으로 만든 Redis 연결 객체를 주입한다.
		redisTemplate.setConnectionFactory(connectionFactory);

		// Redis Key를 문자열로 저장한다.
		redisTemplate.setKeySerializer(new StringRedisSerializer());

		// Redis Value를 문자열로 저장한다.
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		// Redis Hash Key를 문자열로 저장한다.
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());

		// Redis Hash Value를 문자열로 저장한다.
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		// 위 설정들을 RedisTemplate에 최종 적용한다.
		redisTemplate.afterPropertiesSet();

		return redisTemplate;
	}
}