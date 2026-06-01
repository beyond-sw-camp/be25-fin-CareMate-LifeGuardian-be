package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface SalesService {
    SalesSummaryResponseDto getSalesSummary();
}
