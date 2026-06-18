package com.caremate.lifeguardian.recommendation.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.recommendation.dto.response.RecommendationResponse;
import com.caremate.lifeguardian.recommendation.service.RecommendationRedisService;
import com.caremate.lifeguardian.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "보험 추천 API", description = "고객 상세 페이지에서 보험 추천 결과를 조회하거나 최초 추천 결과를 생성하는 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class RecommendationController {

	private final RecommendationService recommendationService;
	private final RecommendationRedisService recommendationRedisService;

	@Operation(
			summary = "고객 보험 추천 결과 조회",
			description = "고객 상세 페이지 진입 시 Redis → DB → 추천 엔진 순서로 보험 추천 결과를 조회하거나 생성합니다."
	)
	@GetMapping("/{customerId}/recommendation")
	public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(
			@PathVariable Long customerId
	) {
		Long currentUserId = SecurityUtil.getCurrentUserId();

		log.info(
				"보험 추천 결과 조회 API 요청 수신 - customerId: {}, 요청 영업사원 ID: {}",
				customerId,
				currentUserId
		);

		// 추천 결과 조회 또는 생성
		RecommendationResponse response =
				recommendationService.getOrCreateRecommendation(customerId, currentUserId);

		log.info(
				"보험 추천 결과 조회 API 처리 성공 - customerId: {}, planId: {}, 요청 영업사원 ID: {}",
				customerId,
				response.getPlanId(),
				currentUserId
		);

		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"보험 추천 결과 조회에 성공했습니다.",
						response
				)
		);
	}
}