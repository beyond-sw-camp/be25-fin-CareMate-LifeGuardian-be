package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserStatusUpdateResponse {

    Long id;
    String statusCode;
    String statusName;
}
