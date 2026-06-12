package com.caremate.lifeguardian.report.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {

    private Long customerId;
    private String conversionStatusCode;
    private String reportTypeCode;
    private Long webFormId;
    private Integer reportYear;
}
