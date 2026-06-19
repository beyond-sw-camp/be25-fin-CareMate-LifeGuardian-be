package com.caremate.lifeguardian.report.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.report.dto.request.ReportBulkSendRequest;
import com.caremate.lifeguardian.report.dto.response.ReportBulkSendResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportSendResultDto;
import com.caremate.lifeguardian.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리포트 API", description = "고객 리포트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class CustomerReportController {

    private final ReportService reportService;

    @Operation(summary = "고객 리포트 개별 발송", description = "리포트 개별 발송용입니다.")
    @PostMapping("/{customerId}/send")
    public ResponseEntity<ApiResponse<ReportSendResultDto>> sendReport(
            @PathVariable Long customerId,
            @RequestParam(required = false) String conversionStatusCode,
            HttpServletRequest request
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ReportSendResultDto response = reportService.sendReport(
                customerId,
                conversionStatusCode,
                currentUserId,
                resolveClientIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, "고객 리포트 발송에 성공했습니다.", response)
        );
    }

    @Operation(summary = "담당 고객 리포트 전체 발송 및 재발송")
    @PostMapping("/send/bulk")
    public ResponseEntity<ApiResponse<ReportBulkSendResultDto>> sendReportsInBulk(
            @RequestBody(required = false) ReportBulkSendRequest bulkSendRequest,
            HttpServletRequest request
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ReportBulkSendResultDto response = reportService.sendReportsInBulk(
                currentUserId,
                resolveClientIp(request),
                request.getHeader("User-Agent"),
                bulkSendRequest == null ? null : bulkSendRequest.getReportIds()
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, "고객 리포트 일괄 발송에 성공했습니다.", response)
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
