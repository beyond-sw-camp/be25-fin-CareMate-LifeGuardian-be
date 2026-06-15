package com.caremate.lifeguardian.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class AuditLogSearchRequest {
    String startDate;
    String endDate;
    String actionTypeCode;
    Integer page;
    Integer size;

    public int getOffset() {
        int currentPage = (page != null && page > 0) ? page : 1;
        return (currentPage - 1) * getSafeSize();
    }

    public int getSafeSize() {
        return (size != null && size > 0) ? size : 10;
    }
}
