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

		validateCustomerOwner(customerId, currentUserId);

		RecommendationResponse cached = redisService.getLatest(customerId);

		if (cached != null) {
			log.info("추천 결과 Redis Cache Hit - customerId: {}, planId: {}", customerId, cached.getPlanId());
			return cached;
		}

		RecommendationResult result = recommendationEngine.run(customerId);

		recommendationMapper.insertInsurancePlan(result.getInsurancePlan());

		Long planId = result.getInsurancePlan().getId();

		recommendationMapper.insertPlanCoverages(planId, result.getSelectedCoverages());

		recommendationMapper.insertRecommendationLog(
				RecommendationLog.from(result, planId)
		);

		RecommendationResponse response =
				RecommendationResponse.from(result, planId);

		redisService.saveLatest(customerId, response);

		return response;
	}

	private void validateCustomerOwner(Long customerId, Long currentUserId) {

		boolean isPotentialCustomer =
				recommendationMapper.existsPotentialCustomer(customerId);

		boolean isIntegratedCustomer =
				recommendationMapper.existsIntegratedCustomer(customerId);

		if (!isPotentialCustomer && !isIntegratedCustomer) {
			throw new BaseException(404, "고객 정보가 존재하지 않습니다.");
		}

		int count;

		if (isPotentialCustomer) {
			count = recommendationMapper.countPotentialCustomerBySalesUser(customerId, currentUserId);
		} else {
			count = recommendationMapper.countIntegratedCustomerBySalesUser(customerId, currentUserId);
		}

		if (count == 0) {
			throw new BaseException(403, "해당 고객의 추천 결과를 조회할 권한이 없습니다.");
		}
	}
}