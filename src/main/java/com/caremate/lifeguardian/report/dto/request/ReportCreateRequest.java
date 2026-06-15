package com.caremate.lifeguardian.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportCreateRequest {

    @Schema(description = "고객 ID", example = "22")
    private Long customerId;

    @Schema(description = "고객 유형 코드 (01: 잠재 고객, 02: 통합 고객)", example = "01")
    private String conversionStatusCode;

    @Schema(description = "리포트 유형 코드 (01: 성장 리포트, 02: 질병 통계 리포트)", example = "01")
    private String reportTypeCode;

    @Schema(description = "웹폼 응답 ID", example = "1", nullable = true)
    private Long webFormId;

    @Schema(description = "리포트 기준 연도", example = "2026")
    private Integer reportYear;
}
