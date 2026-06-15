package com.caremate.lifeguardian.admin.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesPerformerInfo {
    int rank;
    String employeeId;
    String employeeName;
    int contractCount;
}
