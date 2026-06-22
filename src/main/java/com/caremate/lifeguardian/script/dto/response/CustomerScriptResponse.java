package com.caremate.lifeguardian.script.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CustomerScriptResponse {

	private Long scriptId;
	private Long actionItemId;
	private Long customerId;
	private String conversionStatusCode;
	private String triggerTypeCode;
	private String triggerName;
	private String scriptContent;
	private LocalDateTime createdAt;
}