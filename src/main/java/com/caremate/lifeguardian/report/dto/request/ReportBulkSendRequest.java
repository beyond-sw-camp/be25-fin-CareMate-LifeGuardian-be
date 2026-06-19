package com.caremate.lifeguardian.report.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportBulkSendRequest {

    // 선택 발송 대상 리포트 ID 목록. 비어 있으면 전체 발송으로 처리한다.
    private List<Long> reportIds;
}
