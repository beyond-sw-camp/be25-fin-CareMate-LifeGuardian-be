package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserRegisterResponse {
    Long id;
    String employeeId;
    String temporaryPassword;
}
