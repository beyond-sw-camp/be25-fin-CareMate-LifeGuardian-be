package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserInfo {
    // 목록 내 개별 영업사원 정보를 담는 DTO
    Long id;
    String employeeId;
    String name;
    String statusCode;
    String statusName;
    Integer customerCount;
}
