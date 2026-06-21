package com.caremate.lifeguardian.script.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ScriptContextDto {

	private Long actionItemId;
	private Long customerId;
	private String conversionStatusCode;
	private String triggerTypeCode;
	private String triggerName;

	private String childName;
	private LocalDate childBirthDate;
	private String childGender;

	private String parentName;
	private LocalDate parentBirthDate;
	private String parentGender;
}