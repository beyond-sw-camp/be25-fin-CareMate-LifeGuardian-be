package com.caremate.lifeguardian.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CustomerInfoDto {

	private Long customerId;
	private String name;
	private LocalDate birthDate;
	private String gender;
	private Long salesUserId;
}