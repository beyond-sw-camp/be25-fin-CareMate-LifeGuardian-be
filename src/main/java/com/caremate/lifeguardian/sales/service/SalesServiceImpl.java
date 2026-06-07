package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.sales.dto.request.SalesSearchRequestDto;
import com.caremate.lifeguardian.sales.dto.response.SalesListResponseDto;
import com.caremate.lifeguardian.sales.dto.response.SalesSummaryResponseDto;
import com.caremate.lifeguardian.sales.mapper.SalesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    @Override
    public SalesPageResponseDto getSalesList(SalesSearchRequestDto request) {
        // 검색 조건을 먼저 검증하고, 잘못된 값이 있으면 400 예외 발생
        validateSalesListRequest(request);

        try {
            // 검색 조건에 맞는 전체 고객 수를 조회해 페이지 정보를 계산
            long totalCount = salesMapper.countSalesList(request);
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

            // 조회 결과가 없으면 목록 쿼리를 추가로 실행하지 않는다.
            List<SalesListResponseDto> content = totalCount == 0
                    ? Collections.emptyList()
                    : salesMapper.getSalesList(request);

            return SalesPageResponseDto.builder()
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalCount(totalCount)
                    .totalPages(totalPages)
                    .items(content)
                    .build();
        } catch (DataAccessException e) {
            // SQL 실행 또는 DB 연결 실패를 사용자용 500 예외로 변환
            throw new BaseException(
                    500,
                    "시스템 오류로 인해 정보를 조회하지 못했습니다. 관리자에게 문의하세요."
            );
        }
    }

    private void validateSalesListRequest(SalesSearchRequestDto request) {
        if (request == null) {
            throw new BaseException(400, "검색 조건은 필수입니다.");
        }

        if (request.getSalesUserId() == null || request.getSalesUserId() < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }
        if (request.getGender() != null
                && !request.getGender().isBlank()
                && !Set.of("Male", "Female").contains(request.getGender())) {
            throw new BaseException(400, "성별은 Male 또는 Female만 입력할 수 있습니다.");
        }
        if (request.getAge() != null && request.getAge() < 0) {
            throw new BaseException(400, "나이는 0 이상이어야 합니다.");
        }
        if (request.getPage() < 1) {
            throw new BaseException(400, "페이지 번호는 1 이상이어야 합니다.");
        }
        if (request.getSize() < 1) {
            throw new BaseException(400, "페이지 크기는 1 이상이어야 합니다.");
        }
    }
}
