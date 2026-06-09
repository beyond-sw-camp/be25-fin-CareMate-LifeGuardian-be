package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.sales.dto.request.SalesSearchRequestDto;
import com.caremate.lifeguardian.sales.dto.response.SalesPageResponseDto;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;

public interface SalesService {
    SalesSummaryResponseDto getSalesSummary(Long salesUserId, String targetYearMonth);
    SalesPageResponseDto getSalesList(Long currentUserId, SalesSearchRequestDto request);

}
