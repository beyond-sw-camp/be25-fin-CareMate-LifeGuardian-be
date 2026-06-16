package com.caremate.lifeguardian.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateReasonResult {

	private String recommendReason;
	private String scriptData;
}