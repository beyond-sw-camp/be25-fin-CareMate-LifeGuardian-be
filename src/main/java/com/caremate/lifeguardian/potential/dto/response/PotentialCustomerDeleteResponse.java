package com.caremate.lifeguardian.potential.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PotentialCustomerDeleteResponse {

    /**
     * 삭제된 잠재고객 ID
     */
    private Long potentialCustomerId;

    /**
     * 삭제 처리 일시
     */
    private LocalDateTime deletedAt;
}
