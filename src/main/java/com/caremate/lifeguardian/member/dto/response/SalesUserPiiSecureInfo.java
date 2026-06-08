package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserPiiSecureInfo {
    String employeeId;
    String retiredAt;
    String purgedAt;
    long remainingDays;
    String statusName;
}
