package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.request.AuditLogSearchRequest;
import com.caremate.lifeguardian.admin.dto.response.AuditLogResponse;

public interface AuditLogService {
    // 민감 정보 열람 및 감사 로그 조회 API
    AuditLogResponse getAuditLogs(AuditLogSearchRequest request);
}
