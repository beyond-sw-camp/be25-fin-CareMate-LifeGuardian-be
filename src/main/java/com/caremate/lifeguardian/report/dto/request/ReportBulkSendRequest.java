package com.caremate.lifeguardian.report.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReportBulkSendRequest {

    // 선택 발송 대상 리포트 ID 목록. 비어 있으면 전체 발송으로 처리한다.
    private List<Long> reportIds;
}
