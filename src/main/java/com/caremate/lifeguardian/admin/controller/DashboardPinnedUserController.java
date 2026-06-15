package com.caremate.lifeguardian.admin.controller;

import com.caremate.lifeguardian.admin.service.BranchStatisticsService;
import com.caremate.lifeguardian.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드 핀 API", description = "대시보드 영업사원 핀 고정/해제 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/pinned-users")
@RequiredArgsConstructor
public class DashboardPinnedUserController {

    private final BranchStatisticsService branchStatisticsService;

    @Operation(summary = "대시보드 영업사원 핀 고정", description = "로그인한 지점장이 특정 영업사원을 대시보드에 핀 고정합니다.")
    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> pinSalesUser(
            @PathVariable("targetUserId") Long targetUserId) {
        
        log.info("영업사원 핀 고정 API 요청 수신 - targetUserId: {}", targetUserId);
        branchStatisticsService.pinSalesUser(targetUserId);
        log.info("영업사원 핀 고정 API 처리 성공 - targetUserId: {}", targetUserId);

        return ResponseEntity.ok(ApiResponse.success(200, "핀 설정이 정상적으로 변경되었습니다."));
    }

    @Operation(summary = "대시보드 영업사원 핀 해제", description = "로그인한 지점장이 특정 영업사원의 대시보드 핀 고정을 해제합니다.")
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unpinSalesUser(
            @PathVariable("targetUserId") Long targetUserId) {
        
        log.info("영업사원 핀 해제 API 요청 수신 - targetUserId: {}", targetUserId);
        branchStatisticsService.unpinSalesUser(targetUserId);
        log.info("영업사원 핀 해제 API 처리 성공 - targetUserId: {}", targetUserId);

        return ResponseEntity.ok(ApiResponse.success(200, "핀 설정이 정상적으로 변경되었습니다."));
    }
}
