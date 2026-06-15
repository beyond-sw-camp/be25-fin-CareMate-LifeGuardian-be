package com.caremate.lifeguardian.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportSendResultDto {
    private Long customerId;
    private String customerName;
    private String sendStatusCode;
    private String sendStatusName;
    private LocalDateTime sentAt;
}
