package com.caremate.lifeguardian.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // DB에서 담보를 조회 -> 이후 점수를 계산 -> 객체에 넣어야 하기 떄문
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageCandidateDto {

	private Long coverageId;
	private String categoryCode;
	private String coverageName;
	private Integer unitPremium;
	private Integer dangerPriorityOrder;

	private Integer baseScore;
	private Integer biasScore;
	private Integer finalScore;
	private Integer selectedOrder;
}