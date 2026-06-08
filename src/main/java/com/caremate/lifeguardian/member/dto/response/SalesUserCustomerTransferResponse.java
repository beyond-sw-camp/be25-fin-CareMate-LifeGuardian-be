package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserCustomerTransferResponse {
    Long fromUserId;
    Long toUserId;
    long transferredPotentialCount;
    long transferredIntegratedCount;
}
