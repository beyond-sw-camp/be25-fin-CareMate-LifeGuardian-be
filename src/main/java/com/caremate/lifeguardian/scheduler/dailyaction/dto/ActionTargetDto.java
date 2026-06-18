package com.caremate.lifeguardian.scheduler.dailyaction.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ActionTargetDto {

	private Long customerId;
	private Long salesUserId;
	private String conversionStatusCode;

	private String childName;
	private LocalDate childBirthDate;

	private Long parentCustomerId;
	private String parentName;
	private LocalDate parentBirthDate;
}