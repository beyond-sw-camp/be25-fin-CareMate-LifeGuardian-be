package com.caremate.lifeguardian.recommendation.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.recommendation.domain.RecommendationLog;
import com.caremate.lifeguardian.recommendation.dto.RecommendationResult;
import com.caremate.lifeguardian.recommendation.dto.response.RecommendationResponse;
import com.caremate.lifeguardian.recommendation.mapper.RecommendationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

	private final RecommendationRedisService redisService;
	private final RecommendationMapper recommendationMapper;
	private final RecommendationEngine recommendationEngine;

	@Override
	@Transactional
	public RecommendationResponse getOrCreateRecommendation(Long customerId, Long currentUserId) {

		// 로그인한 영업사원이 해당 고객을 담당하고 있는지 검증한다.
		validateCustomerOwner(customerId, currentUserId);

		// Redis에 저장된 최신 추천 결과를 먼저 조회한다.
		RecommendationResponse cached = redisService.getLatest(customerId);

		if (cached != null) {
			log.info("추천 결과 Redis Cache Hit - customerId: {}, planId: {}", customerId, cached.getPlanId());

			return cached;
		}

		// Redis에 추천 결과가 없으면 추천 엔진을 실행한다.
		RecommendationResult result =
				recommendationEngine.run(customerId);

		// 추천 엔진이 생성한 최종 보험 플랜을 DB에 저장한다.
		recommendationMapper.insertInsurancePlan(result.getInsurancePlan());

		Long planId = result.getInsurancePlan().getId();

		// 추천 플랜에 포함된 담보 목록을 저장한다.
		recommendationMapper.insertPlanCoverages(planId, result.getSelectedCoverages());

		// 추천 결과 이력을 저장한다.
		recommendationMapper.insertRecommendationLog(
				RecommendationLog.from(result, planId)
		);

		// 프론트에 반환할 응답 DTO를 생성한다.
		RecommendationResponse response =
				RecommendationResponse.from(result, planId);

		// 생성된 추천 결과를 Redis에 캐싱한다.
		redisService.saveLatest(customerId, response);

		return response;
	}


	private void validateCustomerOwner(Long customerId, Long currentUserId) {
		int count = recommendationMapper.countCustomerBySalesUser(customerId, currentUserId);

		if (count == 0) {
			throw new BaseException(403, "해당 고객의 추천 결과를 조회할 권한이 없습니다.");
		}
	}
}