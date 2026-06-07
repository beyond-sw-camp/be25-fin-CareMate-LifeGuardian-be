package com.caremate.lifeguardian.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.Alias;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("SalesKPI")
public class SalesSummaryResponseDto {

    private Integer year;

    private Integer month;

    private Integer targetCount;

    private Long contractCount;

    private Double achievementRate;

}
