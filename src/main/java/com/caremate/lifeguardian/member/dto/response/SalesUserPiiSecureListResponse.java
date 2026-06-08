package com.caremate.lifeguardian.member.dto.response;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class SalesUserPiiSecureListResponse {
    long totalElements;
    int totalPages;
    List<SalesUserPiiSecureInfo> content;
}
