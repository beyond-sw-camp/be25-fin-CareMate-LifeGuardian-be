package com.caremate.lifeguardian.recommendation.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WebformResponse {

	private Long id;
	private Long customerId;
	private String conversionStatusCode;
	private BigDecimal height;
	private BigDecimal weight;
	private String selectedPriorityCategory;
	private String historyJson;
	private String activityJson;
	private Boolean pastSurgeryOrHospitalization;
	private String desiredBudgetCode;
	private LocalDateTime receivedAt;
}