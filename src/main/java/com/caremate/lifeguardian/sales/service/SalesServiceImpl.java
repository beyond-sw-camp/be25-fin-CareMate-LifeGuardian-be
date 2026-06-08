package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import com.caremate.lifeguardian.sales.mapper.SalesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesMapper salesMapper;

    /*
    - 영업현황 KPI 조회
    - 404, 500 예외처리
     */
    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponseDto getSalesSummary(Long currentUserId, String targetYearMonth) {
        validateSalesSummaryRequest(targetYearMonth);

        SalesSummaryResponseDto salesSummary;
        try {
            salesSummary = salesMapper.getSalesSummary(currentUserId, targetYearMonth);
        } catch (DataAccessException e) {
            throw new BaseException(500, "시스템 오류로 인해 정보를 조회하지 못했습니다. 관리자에게 문의하세요.");
        }

        // 영업 목표치 없을 시
        if (salesSummary == null) {
            throw new BaseException(404, "해당 월의 영업 목표 정보를 찾을 수 없습니다.");
        }

        return salesSummary;
    }

    private void validateSalesSummaryRequest(String targetYearMonth) {

        int month = Integer.parseInt(targetYearMonth.substring(4, 6));
        // 조회 연도, 달 예외처리
        if (month < 1 || month > 12) {
            throw new BaseException(400, "월은 1부터 12 사이여야 합니다.");
        }
    }
}
