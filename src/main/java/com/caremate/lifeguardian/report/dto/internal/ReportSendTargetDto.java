package com.caremate.lifeguardian.report.dto.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportSendTargetDto {
    private Long reportId;
    private Long customerId;
    private String conversionStatusCode;
    private String customerName;
    private String reportUrl;
    private String sendStatusCode;
}
