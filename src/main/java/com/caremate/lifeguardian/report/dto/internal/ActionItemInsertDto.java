package com.caremate.lifeguardian.report.dto.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Alias("insertActionItemDto")
/*
- insert용
 */
public class ActionItemInsertDto {
    private Long id;

    private Long currentUserId;

    // 실제로는 잠재/통합 고객 ID 둘 다 담을 수 있는 target customer id 역할
    private Long customerId;

    // 잠재, 통합고객
    private String conversionStatusCode;

    // 웹폼 제출, 정기 팔로업
    private String triggerTypeCode;

    private Integer priorityScore;

    private LocalDate targetDate;
}
