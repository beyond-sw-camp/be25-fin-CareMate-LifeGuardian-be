package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SalesUserListResponse {
    // 최종 페이징된 응답 데이터를 내려주는 DTO
    Long totalElements;
    Integer totalPages;
    List<SalesUserInfo> content;
}
