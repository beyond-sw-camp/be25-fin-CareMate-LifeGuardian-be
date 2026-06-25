package com.caremate.lifeguardian.sales.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Alias("SalesList")
public class SalesListResponseDto {

    // 고객 정보
    private Long customerId;

    private String customerName;

    private String gender;

    private Integer age;

    private LocalDate birthDate;

    private LocalDate insuranceAgeShiftDate;

    private Integer ageIncreaseDDay;

    private String ageChangeLabel;

    // 고객 단계
    private String customerStageCode;

    private String customerStageName;

    // 3Step
    private String threeStepCode;

    private String threeStepName;

    // 상담 상태
    private String consultStatusCode;

    private String consultStatusName;

    // 계약 현황
    private Long contractId;

    private String contractStatusCode;

    private String contractStatusName;

    // 보험명
    private String insuranceName;

    // 피보험자
    private String insuredName;

    private Long webFormId;

    private String webFormStatusCode;

    private String webFormStatusName;

    // 웹폼 회수일
    private String webformReceivedAt;

    // 리포트
    private Long reportId;

    private String reportUrl;

    private Boolean hasReport; // 존재 여부

    private String reportStatusCode;

    private String reportStatusName;

    private LocalDateTime reportSentAt;

    private Boolean canSendReport; // 리포트 버튼

    // 정렬
    private Integer sortRank;
}
