package com.caremate.lifeguardian.sales.mapper;

import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesMapper {

    SalesSummaryResponseDto getSalesSummary(
            @Param("id") Long id,
            @Param("targetYearMonth") String targetYearMonth
    );
}
