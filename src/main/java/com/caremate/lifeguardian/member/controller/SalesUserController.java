package com.caremate.lifeguardian.member.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.member.dto.request.*;
import com.caremate.lifeguardian.member.dto.response.*;
import com.caremate.lifeguardian.member.service.SalesUserService;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "영업사원 목록 조회 (인사 관리용)", description = "관리자가 영업사원 목록을 키워드 검색 및 상태 필터링을 지원하여 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SalesUserListResponse>> getSalesUserList(
            @ModelAttribute SalesUserSearchRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("영업사원 목록 조회 API 요청 수신 - 요청 관리자 ID: {}", currentUserId);
        SalesUserListResponse response = salesUserService.getSalesUserList(request);
        log.info("영업사원 목록 조회 API 처리 성공 - 조회 건수: {}, 요청 관리자 ID: {}", response.getContent().size(), currentUserId);

        return ResponseEntity
                .ok(ApiResponse.success(200, "영업사원 목록 조회에 성공했습니다.", response));
    }

    @Operation(summary = "영업사원 상태 변경 (인사 관리용)", description = "관리자가 특정 영업사원의 계정 상태를 변경합니다. 비활성('02') 처리 시 잔여 고객 존재 여부를 엄격히 검증합니다.")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<SalesUserStatusUpdateResponse>> changeSalesUserStatus(
            @PathVariable Long userId,
            @Validated @RequestBody SalesUserStatusUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("영업사원 상태 변경 API 요청 수신 - 대상 userId: {}, statusCode: {}, 요청 관리자 ID: {}", userId, request.getStatusCode(), currentUserId);

        SalesUserStatusUpdateResponse response = salesUserService.changeSalesUserStatus(userId, request);
        log.info("영업사원 상태 변경 API 처리 성공 - 대상 userId: {}, 요청 관리자 ID: {}", userId, currentUserId);

        return ResponseEntity
                .ok(ApiResponse.success(200, "계정 상태가 성공적으로 변경되었습니다.", response));
    }


    @Operation(summary = "퇴사자 계정 비활성화 및 세션 파기 (인사 관리용)", description = "관리자가 특정 영업사원을 영구 퇴사 처리하고 개인정보를 보안 격리 및 기기 세션을 일괄 파기합니다.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<SalesUserRetireResponse>> retireSalesUser(
            @PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("퇴사자 계정 비활성화 API 요청 수신 - 대상 userId: {}, 요청 관리자 ID: {}", userId, currentUserId);

        SalesUserRetireResponse response = salesUserService.retireSalesUser(userId);
        log.info("퇴사자 계정 비활성화 API 처리 성공 - 대상 userId: {}, 요청 관리자 ID: {}", userId, currentUserId);

        return ResponseEntity
                .ok(ApiResponse.success(200, "영업사원 퇴사 처리 및 기기 세션 만료가 정상적으로 완료되었습니다.", response));
    }

    @Operation(summary = "퇴사자 고객 일괄 이관 (인사 관리용)", description = "관리자가 퇴사 예정자가 담당하고 있는 모든 잔여 고객의 소유권을 다른 영업사원에게 일괄 이관합니다.")
    @PostMapping("/{userId}/transfer-customers")
    public ResponseEntity<ApiResponse<SalesUserCustomerTransferResponse>> transferCustomers(
            @PathVariable Long userId,
            @Valid @RequestBody SalesUserCustomerTransferRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("퇴사자 고객 일괄 이관 API 요청 수신 - 기존 userId: {}, 인계받을 toUserId: {}, 요청 관리자 ID: {}", userId, request.getToUserId(), currentUserId);

        SalesUserCustomerTransferResponse response = salesUserService.transferCustomers(userId, request, currentUserId);
        log.info("퇴사자 고객 일괄 이관 API 처리 성공 - 기존 userId: {}, 인계받을 toUserId: {}, 요청 관리자 ID: {}", userId, request.getToUserId(), currentUserId);

        String message = String.format("총 %d명의 고객이 성공적으로 이관되었습니다.", response.getTransferredPotentialCount());

        return ResponseEntity
                .ok(ApiResponse.success(200, message, response));
    }


    @Operation(summary = "퇴사자 PII 분리 보관 현황 조회 (인사 관리용)", description = "관리자 대시보드에서 분리 보관 중인 퇴사자 PII 보존 현황 및 남은 파기 일수를 조회합니다.")
    @GetMapping("/pii-secure")
    public ResponseEntity<ApiResponse<SalesUserPiiSecureListResponse>> getPiiSecureList(
            @ModelAttribute SalesUserPiiSecureSearchRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("퇴사자 PII 분리 보관 현황 조회 API 요청 수신 - 요청 관리자 ID: {}", currentUserId);

        SalesUserPiiSecureListResponse response = salesUserService.getPiiSecureList(request);
        log.info("퇴사자 PII 분리 보관 현황 조회 API 처리 성공 - 건수: {}, 요청 관리자 ID: {}", response.getContent().size(), currentUserId);

        return ResponseEntity
                .ok(ApiResponse.success(200, "퇴사자 PII 분리 보관 현황 조회가 완료되었습니다.", response));
    }

}
