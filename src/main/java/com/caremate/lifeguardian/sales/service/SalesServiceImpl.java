package com.caremate.lifeguardian.sales.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.sales.dto.request.SalesSearchRequestDto;
import com.caremate.lifeguardian.sales.dto.response.SalesListResponseDto;
import com.caremate.lifeguardian.sales.dto.response.SalesPageResponseDto;
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
    - 로그인한 영업사원 ID와 조회 연월로 월간 목표 및 계약 성과를 조회
    - 조회 연월 형식과 월 범위를 검증하고, 목표 정보가 없으면 404 예외를 반환
    - DB 조회 중 오류가 발생하면 500 예외를 반환
     */
    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponseDto getSalesSummary(Long currentUserId, String targetYearMonth) {
        validateSalesSummaryRequest(currentUserId, targetYearMonth);

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

    private void validateSalesSummaryRequest(Long currentUserId, String targetYearMonth) {
        if (currentUserId == null || currentUserId < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }

        if (targetYearMonth == null || !targetYearMonth.matches("\\d{6}")) {
            throw new BaseException(400, "조회 연월은 yyyyMM 형식이어야 합니다.");
        }

        int month = Integer.parseInt(targetYearMonth.substring(4, 6));
        // 조회 연도, 달 예외처리
        if (month < 1 || month > 12) {
            throw new BaseException(400, "월은 1부터 12 사이여야 합니다.");
        }
    }

    /*
    - 영업현황 목록 조회
    - 로그인한 영업사원 ID로 고객 목록을 제한하고 검색 조건을 적용
    - 전체 건수 조회 후 페이지 정보를 계산하고, 조회 결과를 페이지 응답으로 조립
    - 검색 조건이 잘못되면 400, DB 조회 중 오류가 발생하면 500 예외를 반환
     */
    @Override
    @Transactional(readOnly = true)
    public SalesPageResponseDto getSalesList(Long currentUserId, SalesSearchRequestDto request) {
        // 검색 조건을 먼저 검증하고, 잘못된 값이 있으면 400 예외 발생
        validateSalesListRequest(currentUserId, request);

        try {
            // 검색 조건에 맞는 전체 고객 수를 조회해 페이지 정보를 계산
            long totalCount = salesMapper.countSalesList(currentUserId, request);
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

            // 조회 결과가 없으면 목록 쿼리를 추가로 실행하지 않는다.
            List<SalesListResponseDto> content = totalCount == 0
                    ? Collections.emptyList()
                    : salesMapper.getSalesList(currentUserId, request);

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

    private void validateSalesListRequest(Long currentUserId, SalesSearchRequestDto request) {
        if (request == null) {
            throw new BaseException(400, "검색 조건은 필수입니다.");
        }

        // 영업현황 목록은 로그인한 영업사원 기준으로만 조회
        if (currentUserId == null || currentUserId < 1) {
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
        if (request.getSize() > 100) {
            throw new BaseException(400, "페이지 크기는 100 이하여야 합니다.");
        }
    }
}
