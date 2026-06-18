package com.caremate.lifeguardian.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiseaseRankDto {

	private String dataYear;
	private Integer rankByAgeGender;
}