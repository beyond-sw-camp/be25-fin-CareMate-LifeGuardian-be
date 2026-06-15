package com.caremate.lifeguardian.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class AuditLogResponse {
    long totalElements;
    int totalPages;
    List<AuditLogInfo> content;
}
