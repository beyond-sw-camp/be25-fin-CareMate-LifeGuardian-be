package com.caremate.lifeguardian.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 요약 조회 응답 DTO
 *
 * 조회 데이터:
 * - 잠재고객 상담 상태별 고객 수
 * - 계약 진행 상태별 고객 수
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대시보드 요약 조회 응답")
public class DashboardSummaryResponse {

    @Schema(description = "잠재고객 미상담 고객 수", example = "20")
    private Integer uncontactedCustomerCount;

    @Schema(description = "잠재고객 상담중 고객 수", example = "30")
    private Integer consultingCustomerCount;

    @Schema(description = "설계중 고객 수", example = "10")
    private Integer designingContractCount;

    @Schema(description = "설계완료 고객 수", example = "25")
    private Integer designedContractCount;

    @Schema(description = "청약중 고객 수", example = "25")
    private Integer subscriptionInProgressCount;

    @Schema(description = "청약완료 고객 수", example = "20")
    private Integer subscriptionCompletedCount;

    @Schema(description = "수납완료 고객 수", example = "13")
    private Integer paymentCompletedCount;

    @Schema(description = "계약완료 고객 수", example = "105")
    private Integer contractCompletedCount;
}
