package com.caremate.lifeguardian.recommendation.domain;


import com.caremate.lifeguardian.recommendation.dto.RecommendationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationLog {

	private Long id;

	private Long potentialCustomerId;

	private String targetInsuredType;

	private Long webformResponseId;

	private Long salesUserId;

	private String recommendedCategoryCode;

	private Integer totalScore;

	private String recommendReason;

	private String scriptData;

	private Long insurancePlanId;

	public static RecommendationLog from(
			RecommendationResult result,
			Long planId
	) {
		return RecommendationLog.builder()
				.potentialCustomerId(result.getCustomerId())
				.targetInsuredType("01")
				.webformResponseId(result.getWebformResponse().getId())
				.salesUserId(result.getCustomer().getSalesUserId())
				.recommendedCategoryCode(result.getMainCategoryCode())
				.totalScore(result.getTotalScore())
				.recommendReason(result.getRecommendReason())
				.scriptData(result.getScriptData())
				.insurancePlanId(planId)
				.build();
	}
}