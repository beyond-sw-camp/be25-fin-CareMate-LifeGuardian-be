package com.caremate.lifeguardian.recommendation.dto;

import com.caremate.lifeguardian.recommendation.domain.InsurancePlan;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendationResult {

	private Long customerId;
	private CustomerInfoDto customer;
	private WebformResponse webformResponse;
	private String mainCategoryCode;
	private List<String> subCategoryCodes;
	private List<CoverageCandidateDto> selectedCoverages;
	private InsurancePlan insurancePlan;
	private Integer totalScore;
	private String recommendReason;
	private String scriptData;
}