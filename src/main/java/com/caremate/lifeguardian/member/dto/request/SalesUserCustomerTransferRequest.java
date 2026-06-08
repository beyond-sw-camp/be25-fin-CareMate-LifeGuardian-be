package com.caremate.lifeguardian.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUserCustomerTransferRequest {

    @NotNull(message = "인계받을 대상 영업사원 ID는 필수입니다.")
    private Long toUserId;
}
