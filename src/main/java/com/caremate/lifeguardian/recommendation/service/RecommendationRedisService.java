package com.caremate.lifeguardian.recommendation.service;

import com.caremate.lifeguardian.common.redis.RedisKeyGenerator;
import com.caremate.lifeguardian.common.redis.RedisService;
import com.caremate.lifeguardian.recommendation.dto.response.RecommendationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationRedisService {

	private final RedisService redisService;
	private final ObjectMapper objectMapper;

	private static final long RECOMMENDATION_CACHE_TTL_SECONDS = 60 * 30;

	public RecommendationResponse getLatest(Long customerId) {

		// 고객 ID 기반 Redis Key 생성
		String key = RedisKeyGenerator.recommendation(customerId);

		// Redis에서 추천 결과 JSON 조회
		String json = redisService.get(key);

		// 캐시가 없으면 추천 엔진을 실행할 수 있도록 null 반환
		if (json == null) {
			return null;
		}

		try {
			// JSON 문자열을 응답 DTO로 변환
			return objectMapper.readValue(json, RecommendationResponse.class);
		} catch (JsonProcessingException e) {
			// DTO 구조 변경 등으로 역직렬화에 실패한 경우
			// 잘못된 캐시를 삭제하고 추천 엔진이 다시 실행되도록 null 반환
			redisService.delete(key);
			return null;
		}
	}

	public void saveLatest(Long customerId, RecommendationResponse response) {
		try {
			// 고객 ID 기반 Redis Key 생성
			String key = RedisKeyGenerator.recommendation(customerId);

			// 응답 DTO를 JSON 문자열로 변환
			String json = objectMapper.writeValueAsString(response);

			// Redis에 추천 결과 저장
			// TTL이 지나면 자동 만료되어 다음 조회 시 추천 엔진이 다시 실행된다.
			redisService.save(
					key,
					json,
					RECOMMENDATION_CACHE_TTL_SECONDS
			);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("추천 결과 Redis 직렬화에 실패했습니다.", e);
		}
	}
	public void deleteLatest(Long customerId) {
		String key = RedisKeyGenerator.recommendation(customerId);
		redisService.delete(key);

		log.info(
				"추천 결과 Redis 캐시 삭제 완료 - customerId: {}, key: {}",
				customerId,
				key
		);
	}
}