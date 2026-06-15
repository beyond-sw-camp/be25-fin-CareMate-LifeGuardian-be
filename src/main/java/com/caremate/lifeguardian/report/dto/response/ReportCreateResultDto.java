package com.caremate.lifeguardian.report.dto.response;

import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportCreateResultDto {

    private Long customerId;
    private String conversionStatusCode; // 고객 유형
    private String reportTypeCode; // 리포트 유형
    private Integer reportYear; // 생성일시

    private Long actionItemId;
    private Long reportId;
    private String reportUrl;
    private String sendStatusCode;
    private boolean success;
    private String errorMessage;

    public static ReportCreateResultDto success(
            ReportTargetDto target,
            Long actionItemId,
            CustomerReportInsertDto report
    ) {
        return ReportCreateResultDto.builder()
                .customerId(target.getCustomerId())
                .conversionStatusCode(report.getConversionStatusCode())
                .reportTypeCode(report.getReportTypeCode())
                .reportYear(target.getReportYear())
                .actionItemId(actionItemId)
                .reportId(report.getId())
                .reportUrl(report.getReportUrl())
                .sendStatusCode(report.getSendStatusCode())
                .success(true)
                .build();
    }

    public static ReportCreateResultDto fail(ReportTargetDto target, String errorMessage) {
        return ReportCreateResultDto.builder()
                .customerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .reportTypeCode(target.getReportTypeCode())
                .reportYear(target.getReportYear())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
