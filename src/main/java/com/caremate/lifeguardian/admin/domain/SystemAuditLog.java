package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class SystemAuditLog {
    Long auditId;
    Long salesUserId;
    Long targetCustomerId;
    String actionTypeCode;
    String ipAddress;
    String userAgent;
    String reason;
    LocalDateTime createdAt;
}
