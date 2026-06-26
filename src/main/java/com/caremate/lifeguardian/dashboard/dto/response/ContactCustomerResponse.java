package com.caremate.lifeguardian.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 오늘 연락 고객 목록 조회 응답 DTO
 *
 * 잠재고객 중 오늘 연락이 필요한 고객 정보를 내려준다.
 *
 * 목록 노출 조건:
 * - 오늘 생일인 잠재고객
 * - 상령일 D-30인 잠재고객
 * - 상령일 D-7인 잠재고객
 * - 상령일 D-DAY인 잠재고객
 * - 3step Case A 대상 잠재고객
 *
 * 버튼 제어:
 * - 생일인 고객만 웹폼 발송 가능
 * - 상령일 D-DAY 고객만 리포트 발송 가능
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "오늘 연락 고객 목록 조회 응답")
public class ContactCustomerResponse {

    @Schema(description = "액션 아이템 ID", example = "1")
    private Long actionItemId;

    @Schema(description = "잠재고객 ID", example = "1")
    private Long potentialCustomerId;

    @Schema(description = "고객명", example = "김민수")
    private String customerName;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "만 나이", example = "10")
    private Integer age;

    @Schema(description = "생년월일", example = "2016-03-15")
    private LocalDate birthDate;

    @Schema(description = "상령일", example = "2026-06-11")
    private LocalDate insuranceAgeShiftDate;

    @Schema(description = "상령일 표시값", example = "D-DAY")
    private String ageChangeLabel;

    @Schema(description = "3step 뱃지명", example = "가족 통합 리모델링")
    private String badgeName;

    @Schema(description = "3step 색상 구분", example = "RED")
    private String badgeColor;

    @Schema(description = "상담 상태 코드", example = "01")
    private String consultStatusCode;

    @Schema(description = "상담 상태명", example = "미상담")
    private String consultStatusName;

    @Schema(description = "웹폼 발송 가능 여부", example = "true")
    private Boolean webFormSendEnabled;

    @Schema(description = "웹폼 발송 상태 코드", example = "01")
    private String webFormStatusCode;

    @Schema(description = "웹폼 발송 상태명", example = "미발송")
    private String webFormStatusName;

    @Schema(description = "리포트 ID", example = "10")
    private Long reportId;

    @Schema(description = "리포트 발송 가능 여부", example = "true")
    private Boolean reportSendEnabled;

    @Schema(description = "리포트 발송 상태 코드", example = "01")
    private String reportSendStatusCode;

    @Schema(description = "리포트 발송 상태명", example = "발송대기")
    private String reportSendStatusName;

    @Schema(description = "정렬 우선순위", example = "100")
    private Integer priorityScore;

    private String contactReason;
}
