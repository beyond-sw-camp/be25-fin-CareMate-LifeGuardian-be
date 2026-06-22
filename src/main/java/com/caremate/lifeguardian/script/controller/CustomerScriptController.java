package com.caremate.lifeguardian.script.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.script.dto.response.CustomerScriptResponse;
import com.caremate.lifeguardian.script.service.CustomerScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
		name = "고객 상담 스크립트 API",
		description = "고객 상세 페이지의 상담 스크립트 조회 및 자동 생성 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerId}/scripts")
public class CustomerScriptController {

	private final CustomerScriptService customerScriptService;

	@GetMapping
	@Operation(summary = "오늘 액션 기준 고객 상담 스크립트 조회 또는 자동 생성")
	public ResponseEntity<ApiResponse<CustomerScriptResponse>> getOrCreateScript(
			@PathVariable Long customerId
	) {
		Long currentUserId = SecurityUtil.getCurrentUserId();

		CustomerScriptResponse response =
				customerScriptService.getOrCreateTodayScript(
						currentUserId,
						customerId
				);

		if (response == null) {
			return ResponseEntity.ok(
					ApiResponse.success(
							200,
							"오늘 생성할 상담 스크립트 액션 아이템이 없습니다.",
							null
					)
			);
		}

		return ResponseEntity.ok(
				ApiResponse.success(
						200,
						"오늘 상담 스크립트 조회에 성공했습니다.",
						response
				)
		);
	}
}