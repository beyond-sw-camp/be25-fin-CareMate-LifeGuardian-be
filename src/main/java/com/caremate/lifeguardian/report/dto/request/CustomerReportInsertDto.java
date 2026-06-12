package com.caremate.lifeguardian.report.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@Alias("CustomerReportInsertDto")
public class CustomerReportInsertDto {
    private Long id;

    private Long customerId;
    private String conversionStatusCode;

    private Long actionItemId;
    private String reportTypeCode;

    private Long webformResponseId;
    private String reportUrl;
    private String sendStatusCode;


}
