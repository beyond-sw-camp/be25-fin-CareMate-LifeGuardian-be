package com.caremate.lifeguardian.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class AuditLogInfo {
    Long auditId;
    String createdAt;
    Long salesUserId;
    String employeeName;
    String actionName;
    String customerFormattedId;
    String customerName;
    String ipAddress;
}
