package com.caremate.lifeguardian.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportBulkSendResultDto {
    private int requestedCount;
    private int successCount;
    private int skippedCount;
    private int failedCount;
    private LocalDateTime sentAt;
}
