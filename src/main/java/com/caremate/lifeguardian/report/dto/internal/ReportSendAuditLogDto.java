package com.caremate.lifeguardian.report.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportSendAuditLogDto {

    private Long currentUserId;
    private Long targetCustomerId;
    private String conversionStatusCode;
    private String actionTypeCode;
    private String ipAddress;
    private String userAgent;
    private String reason;
}
