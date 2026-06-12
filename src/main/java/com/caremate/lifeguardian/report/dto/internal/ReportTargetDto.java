package com.caremate.lifeguardian.report.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
- internal: 서비스 내부에서만 사용 데이터 / 외부 노출 X
 */
public class ReportTargetDto {

    private Long currentUserId;
    private Long customerId;
    private String conversionStatusCode;
    private String reportTypeCode;
    private Long webFormId;
    private Integer reportYear;
}
