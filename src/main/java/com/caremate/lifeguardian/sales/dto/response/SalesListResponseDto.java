package com.caremate.lifeguardian.sales.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

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

    // 고객 단계
    private String customerStageCode;

    private String customerStageName;

    // 상령일
    private LocalDate insuranceAgeShiftDate; // 상령일 날짜

    private Integer ageIncreaseDDay; // 상령일까지 남은 일수

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

    // 웹폼 회수일
    private String webformReceivedAt;

    // 리포트
    private Long reportId;

    private Boolean hasReport; // 존재 여부

    private String reportStatusCode;

    private String reportStatusName;

    private Boolean canSendReport; // 리포트 버튼

    // 정렬
    private Integer sortRank;
}