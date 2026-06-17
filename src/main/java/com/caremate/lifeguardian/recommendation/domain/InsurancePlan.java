package com.caremate.lifeguardian.recommendation.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlan {

	private Long id;

	private Long webformResponseId;

	private String planName;

	private Integer totalPremium;

	private String scriptData;

	private String recommendationTypeCode;

	private LocalDateTime createdAt;
}