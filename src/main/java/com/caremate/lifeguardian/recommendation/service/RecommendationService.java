package com.caremate.lifeguardian.recommendation.service;

import com.caremate.lifeguardian.recommendation.dto.response.RecommendationResponse;

public interface RecommendationService {

	RecommendationResponse getOrCreateRecommendation(
			Long customerId,
			Long currentUserId
	);
}