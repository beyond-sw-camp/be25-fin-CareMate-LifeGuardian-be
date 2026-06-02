package com.caremate.lifeguardian.member.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.member.dto.request.SalesUserRegisterRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserRegisterResponse;
import com.caremate.lifeguardian.member.service.SalesUserService;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "영업사원 관리 API", description = "관리자(ADMIN) 권한이 필요한 인사 관리용 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/sales-users")
@RequiredArgsConstructor
public class SalesUserController {

    private final SalesUserService salesUserService;

    @Operation(summary = "신입 영업사원 등록", description = "관리자가 새로운 영업사원을 등록하고 자동 채번된 사번 및 임시 비밀번호를 발급받습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<SalesUserRegisterResponse>> registerSalesUser(
            @Valid @RequestBody SalesUserRegisterRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("영업사원 등록 API 요청 수신 - 이름: {}, 요청 관리자 ID: {}", request.getName(), currentUserId);
        SalesUserRegisterResponse response = salesUserService.registerSalesUser(request);
        log.info("영업사원 등록 API 처리 성공 - 사번: {}, 요청 관리자 ID: {}", response.getEmployeeId(), currentUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(ApiResponse.success(201, "신입 영업사원 등록이 완료되었습니다.", response));
    }
}
