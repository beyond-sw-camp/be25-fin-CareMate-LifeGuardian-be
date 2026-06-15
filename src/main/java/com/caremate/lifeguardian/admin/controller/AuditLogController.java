package com.caremate.lifeguardian.admin.controller;

import com.caremate.lifeguardian.admin.dto.request.AuditLogSearchRequest;
import com.caremate.lifeguardian.admin.dto.response.AuditLogResponse;
import com.caremate.lifeguardian.admin.service.AuditLogService;
import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "감사 로그 API", description = "민감 정보 열람 및 감사 로그를 조회하는 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "민감 정보 열람 및 감사 로그 조회", description = "관리자 권한을 가진 사용자가 보안 접속 및 감사 로그를 필터링(조회 기간, 액션 분류 코드)하여 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAuditLogs(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "actionTypeCode", required = false) String actionTypeCode,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        log.info("감사 로그 조회 API 요청 수신 - startDate: {}, endDate: {}, actionTypeCode: {}, page: {}, size: {}",
                startDate, endDate, actionTypeCode, page, size);

        // 컨트롤러 레벨 날짜 형식 정규식 수동 유효성 검증
        List<ErrorResponse> errors = new ArrayList<>();
        if (startDate != null && !startDate.isEmpty() && !startDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            errors.add(ErrorResponse.builder()
                    .field("startDate")
                    .reason("시작일은 YYYY-MM-DD 형식이어야 합니다.")
                    .build());
        }
        if (endDate != null && !endDate.isEmpty() && !endDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            errors.add(ErrorResponse.builder()
                    .field("endDate")
                    .reason("종료일은 YYYY-MM-DD 형식이어야 합니다.")
                    .build());
        }

        if (!errors.isEmpty()) {
            return ResponseEntity
                    .status(400)
                    .body(ApiResponse.fail(400, "날짜 형식이 올바르지 않습니다.", errors));
        }

        AuditLogSearchRequest request = AuditLogSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .actionTypeCode(actionTypeCode)
                .page(page)
                .size(size)
                .build();

        AuditLogResponse response = auditLogService.getAuditLogs(request);
        log.info("감사 로그 조회 API 처리 성공 - 조회 건수: {}", response.getContent().size());

        return ResponseEntity.ok(ApiResponse.success(200, "민감 정보 열람 및 감사 로그 조회가 완료되었습니다.", response));
    }
}
