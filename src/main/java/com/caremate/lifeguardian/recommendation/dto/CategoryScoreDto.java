package com.caremate.lifeguardian.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryScoreDto {

	private String categoryCode;
	private Integer score;
}