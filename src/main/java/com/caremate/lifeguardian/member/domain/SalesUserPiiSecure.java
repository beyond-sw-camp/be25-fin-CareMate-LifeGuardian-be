package com.caremate.lifeguardian.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUserPiiSecure {
    // 퇴사자 PII 격리 테이블 대응
    private String employeeId;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private LocalDateTime retiredAt;
    private LocalDateTime purgedAt;
}
