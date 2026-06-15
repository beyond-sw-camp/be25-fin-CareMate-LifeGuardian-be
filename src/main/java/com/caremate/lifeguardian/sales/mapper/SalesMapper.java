package com.caremate.lifeguardian.sales.mapper;

import com.caremate.lifeguardian.sales.dto.request.SalesSearchRequestDto;
import com.caremate.lifeguardian.sales.dto.response.SalesListResponseDto;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalesMapper {

    SalesSummaryResponseDto getSalesSummary(
            @Param("id") Long id,
            @Param("targetYearMonth") String targetYearMonth
    );

    long countSalesList(
            @Param("currentUserId") Long currentUserId,
            @Param("request") SalesSearchRequestDto request
    );

    List<SalesListResponseDto> getSalesList(
            @Param("currentUserId") Long currentUserId,
            @Param("request") SalesSearchRequestDto request
    );
}
