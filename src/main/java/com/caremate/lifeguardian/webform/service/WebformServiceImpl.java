package com.caremate.lifeguardian.webform.service;

import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.webform.dto.response.WebformSendResponse;
import com.caremate.lifeguardian.webform.mapper.WebformMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebformServiceImpl implements WebformService {

    private final WebformMapper webformMapper;

    /**
     * 웹폼 개별 발송
     */
    @Override
    @Transactional
    public WebformSendResponse sendWebform(Long customerId) {

        Long salesUserId = SecurityUtil.getCurrentUserId();
        String uuidToken = UUID.randomUUID().toString();
        String conversionStatusCode = "01";

        webformMapper.insertWebformIssuance(
                salesUserId,
                customerId,
                conversionStatusCode,
                uuidToken
        );

        return WebformSendResponse.builder()
                .customerId(customerId)
                .conversionStatusCode(conversionStatusCode)
                .uuidToken(uuidToken)
                .webformStatusCode("02")
                .webformStatusName("발송완료")
                .issuedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 웹폼 일괄 발송
     *
     * request로 customerIds를 받지 않고,
     * 백엔드가 직접 ㄷ오늘 웹폼 발송 대상 고객을 조회한다.
     */
    @Override
    @Transactional
    public List<WebformSendResponse> sendBulkWebform() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        List<Long> todayTargetCustomerIds =
                webformMapper.findTodayWebformSendTargetCustomerIds(salesUserId);

        return todayTargetCustomerIds.stream()
                .map(this::sendWebform)
                .toList();
    }

    /**
     * 웹폼 회수 처리
     */
    @Override
    @Transactional
    public void collectWebform(String uuidToken) {

        Long customerId = webformMapper.findCustomerIdByUuidToken(uuidToken);

        if (customerId == null) {
            throw new IllegalArgumentException("유효하지 않은 웹폼 UUID 토큰입니다.");
        }

        int updatedIssuanceCount =
                webformMapper.updateWebformCollected(uuidToken);

        if (updatedIssuanceCount == 0) {
            throw new IllegalStateException("웹폼 회수 처리에 실패했습니다.");
        }

        int updatedCustomerCount =
                webformMapper.updatePotentialCustomerConsultStatus(customerId);

        if (updatedCustomerCount == 0) {
            throw new IllegalStateException("잠재고객 상담 상태 변경에 실패했습니다.");
        }
    }
}
