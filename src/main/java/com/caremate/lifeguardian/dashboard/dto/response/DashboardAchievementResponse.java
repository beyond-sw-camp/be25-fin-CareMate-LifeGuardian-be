package com.caremate.lifeguardian.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영업 달성률 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "영업 달성률 조회 응답")
public class DashboardAchievementResponse {

    @Schema(description = "이번 달 목표 계약 건수", example = "100")
    private Integer targetContractCount;

    @Schema(description = "이번 달 계약 완료 건수", example = "80")
    private Integer completedContractCount;

    @Schema(description = "영업 달성률(%)", example = "80")
    private Integer achievementRate;
}
