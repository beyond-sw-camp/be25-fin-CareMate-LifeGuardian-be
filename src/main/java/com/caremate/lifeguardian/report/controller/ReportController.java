package com.caremate.lifeguardian.report.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.request.ReportCreateRequest;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.service.ReportPreviewService;
import com.caremate.lifeguardian.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "리포트 API", description = "고객 리포트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportPreviewService reportPreviewService;

    @Operation(summary = "R2 리포트 업로드")
    @PostMapping("/preview/r2")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadR2Preview() {
        String reportUrl = reportPreviewService.createAndUploadSample();

        return ResponseEntity.ok(
                ApiResponse.success(200, "고객 리포트 업로드가 완료되었습니다.",
                        Map.of("reportUrl", reportUrl)
                )
        );
    }

    @Operation(summary = "리포트 생성", description = "고객별 리포트와 액션아이템을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<List<ReportCreateResultDto>>> createReports(
            @RequestBody List<ReportCreateRequest> requests
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId(); // 로그인 사용자

        // 리스트의 각 요청 객체를 새로운 ReportTargetDto로 변환
        // 클라이언트 신뢰 X 서버가 신뢰할 수 있는 새 객체 만든다.
        List<ReportTargetDto> targets = requests.stream()
                .map(request -> ReportTargetDto.builder()
                        .currentUserId(currentUserId) // JWT 조회한 로그인 사용자 ID
                        .customerId(request.getCustomerId())
                        .conversionStatusCode(request.getConversionStatusCode())
                        .reportTypeCode(request.getReportTypeCode())
                        .webFormId(request.getWebFormId())
                        .reportYear(request.getReportYear())
                        .build())
                .toList();

        List<ReportCreateResultDto> response = reportService.createReports(targets);

        return ResponseEntity.ok(
                ApiResponse.success(200, "리포트 생성 요청을 보냈습니다.", response)
        );
    }
}
