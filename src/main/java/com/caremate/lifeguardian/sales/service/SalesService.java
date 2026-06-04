package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;

public interface SalesService {
    SalesSummaryResponseDto getSalesSummary(Long salesUserId, String targetYearMonth);
}
