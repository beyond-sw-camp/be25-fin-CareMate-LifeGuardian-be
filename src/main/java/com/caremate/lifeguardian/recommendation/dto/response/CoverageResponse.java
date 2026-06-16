package com.caremate.lifeguardian.recommendation.dto.response;

import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverageResponse {

	private Long coverageId;
	private String coverageName;
	private String categoryCode;
	private Integer premium;
	private Integer baseScore;
	private Integer biasScore;
	private Integer finalScore;
	private Integer selectedOrder;

	public static CoverageResponse from(CoverageCandidateDto coverage) {
		return CoverageResponse.builder()
				.coverageId(coverage.getCoverageId())
				.coverageName(coverage.getCoverageName())
				.categoryCode(coverage.getCategoryCode())
				.premium(coverage.getUnitPremium())
				.baseScore(coverage.getBaseScore())
				.biasScore(coverage.getBiasScore())
				.finalScore(coverage.getFinalScore())
				.selectedOrder(coverage.getSelectedOrder())
				.build();
	}
}
