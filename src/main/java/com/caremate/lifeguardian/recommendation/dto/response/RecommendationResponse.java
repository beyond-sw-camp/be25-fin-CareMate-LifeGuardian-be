package com.caremate.lifeguardian.recommendation.dto.response;

import com.caremate.lifeguardian.recommendation.dto.RecommendationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

	private Long planId;
	private String planName;
	private Integer totalPremium;
	private Integer totalScore;
	private String recommendReason;
	private List<CoverageResponse> coverages;

	public static RecommendationResponse from(
			RecommendationResult result,
			Long planId
	) {
		return RecommendationResponse.builder()
				.planId(planId)
				.planName(result.getInsurancePlan().getPlanName())
				.totalPremium(result.getInsurancePlan().getTotalPremium())
				.totalScore(result.getTotalScore())
				.recommendReason(result.getRecommendReason())
				.coverages(
						result.getSelectedCoverages()
								.stream()
								.map(CoverageResponse::from)
								.toList()
				)
				.build();
	}
}