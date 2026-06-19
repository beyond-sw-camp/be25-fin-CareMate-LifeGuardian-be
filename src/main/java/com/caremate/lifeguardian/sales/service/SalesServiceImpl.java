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

import java.util.ArrayList;
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

        if (salesSummary == null) {
            throw new BaseException(404, "해당 월의 영업 목표 정보를 찾을 수 없습니다.");
        }

        return salesSummary;
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
        validateSalesListRequest(currentUserId, request);

        try {
            long totalCount = salesMapper.countSalesList(currentUserId, request);
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

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
            throw new BaseException(500, "시스템 오류로 인해 정보를 조회하지 못했습니다. 관리자에게 문의하세요.");
        }
    }

    private void validateSalesSummaryRequest(Long currentUserId, String targetYearMonth) {
        if (currentUserId == null || currentUserId < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }

        if (targetYearMonth == null || !targetYearMonth.matches("\\d{6}")) {
            throw new BaseException(400, "조회 년월은 yyyyMM 형식이어야 합니다.");
        }

        int month = Integer.parseInt(targetYearMonth.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new BaseException(400, "월은 1부터 12 사이어야 합니다.");
        }
    }

    private void validateSalesListRequest(Long currentUserId, SalesSearchRequestDto request) {
        if (request == null) {
            throw new BaseException(400, "검색 조건은 필수입니다.");
        }

        request.setConsultStatusCode(normalizeCodes(request.getConsultStatusCode()));
        request.setContractStatusCode(normalizeCodes(request.getContractStatusCode()));
        request.setContractStatusCodes(normalizeCodes(request.getContractStatusCodes()));

        if (currentUserId == null || currentUserId < 1) {
            throw new BaseException(400, "영업사원 ID는 1 이상이어야 합니다.");
        }
        if (request.getCustomerName() != null) {
            String trimmedCustomerName = request.getCustomerName().trim();
            if (trimmedCustomerName.matches(".*\\s+.*")) {
                throw new BaseException(400, "고객명 검색어 중간에는 공백을 입력할 수 없습니다.");
            }
            request.setCustomerName(trimmedCustomerName);
        }
        if (request.getGender() != null
                && !request.getGender().isBlank()
                && !Set.of("Male", "Female").contains(request.getGender())) {
            throw new BaseException(400, "성별은 Male 또는 Female만 입력할 수 있습니다.");
        }
        if (request.getCustomerStageCode() != null
                && !request.getCustomerStageCode().isBlank()
                && !Set.of("01", "02").contains(request.getCustomerStageCode())) {
            throw new BaseException(400, "고객 단계는 01 또는 02만 입력할 수 있습니다.");
        }
        if (hasInvalidCodes(request.getConsultStatusCode(), Set.of("01", "02"))) {
            throw new BaseException(400, "상담 상태는 01 또는 02만 입력할 수 있습니다.");
        }
        if (hasInvalidCodes(request.getContractStatusCode(), Set.of("01", "02", "03", "04", "06"))
                || hasInvalidCodes(request.getContractStatusCodes(), Set.of("01", "02", "03", "04", "06"))) {
            throw new BaseException(400, "계약 상태는 01, 02, 03, 04, 06만 입력할 수 있습니다.");
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
            throw new BaseException(400, "페이지 크기는 100 이하이어야 합니다.");
        }
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalized = new ArrayList<>();
        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            for (String splitCode : code.split(",")) {
                String trimmed = splitCode.trim();
                if (!trimmed.isEmpty()) {
                    normalized.add(trimmed);
                }
            }
        }
        return normalized;
    }

    private boolean hasInvalidCodes(List<String> codes, Set<String> allowedCodes) {
        return codes != null && !codes.isEmpty() && !allowedCodes.containsAll(codes);
    }
}
