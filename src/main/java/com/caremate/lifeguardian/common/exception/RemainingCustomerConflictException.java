package com.caremate.lifeguardian.common.exception;

import lombok.Getter;

@Getter
public class RemainingCustomerConflictException extends BaseException {
    private final long remainingCustomerCount;

    // 퇴사 처리시 잔여 고객이 존재하는 경우 발생시킬 커스텀 예외
    public RemainingCustomerConflictException(long remainingCustomerCount) {
        super(409, "잔여 고객이 존재하여 비활성화(퇴사) 처리가 불가합니다. 고객 이관을 먼저 진행해주세요.");
        this.remainingCustomerCount = remainingCustomerCount;
    }
}
