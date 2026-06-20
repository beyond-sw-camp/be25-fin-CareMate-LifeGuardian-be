package com.caremate.lifeguardian.webform.service;

import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.webform.dto.response.SalesStatusWebformTargetResponse;
import com.caremate.lifeguardian.webform.dto.response.WebformIssuanceTargetResponse;
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
    public WebformSendResponse sendWebform(
            String sendSource,
            String conversionStatusCode,
            Long customerId
    ) {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        // 고객 존재 여부 검증
        validateCustomerExists(
                salesUserId,
                conversionStatusCode,
                customerId
        );

        if ("dashboard".equals(sendSource)) {

            validateDashboardSendTarget(
                    salesUserId,
                    conversionStatusCode,
                    customerId
            );

            boolean alreadySent =
                    webformMapper.existsTodaySentWebform(
                            salesUserId,
                            customerId,
                            conversionStatusCode
                    );

            if (alreadySent) {
                throw new IllegalStateException(
                        "대시보드에서는 당일 재발송이 불가능합니다."
                );
            }
        }

        String uuidToken = UUID.randomUUID().toString();

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
     * 대시보드용 웹폼 일괄 발송
     * <p>
     * 처리 흐름:
     * - 현재 로그인한 영업사원 ID를 가져온다.
     * - 오늘 연락 고객 중 웹폼 발송 대상인 잠재고객 ID 목록을 조회한다.
     * - 조회된 잠재고객에게만 웹폼을 발송한다.
     * <p>
     * 주의:
     * - 대시보드 오늘 연락 고객 목록은 잠재고객 전용이다.
     * - 따라서 conversionStatusCode는 항상 '01'로 저장한다.
     */
    @Override
    @Transactional
    public List<WebformSendResponse> sendBulkWebform() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        List<Long> todayTargetCustomerIds =
                webformMapper.findTodayWebformSendTargetCustomerIds(salesUserId);

        // 발송 대상 없음 검증
        if (todayTargetCustomerIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "오늘 웹폼 발송 대상 고객이 없습니다."
            );
        }

        return todayTargetCustomerIds.stream()
                .map(customerId -> sendWebform("dashboard", "01", customerId))
                .toList();
    }

    /**
     * 영업현황용 웹폼 일괄 발송
     * <p>
     * 잠재고객 + 통합고객 모두에게 웹폼을 발송한다.
     */
    @Override
    @Transactional
    public List<WebformSendResponse> sendSalesStatusBulkWebform() {

        Long salesUserId = SecurityUtil.getCurrentUserId();

        List<SalesStatusWebformTargetResponse> targets =
                webformMapper.findSalesStatusWebformTargets(salesUserId);

        return targets.stream()
                .map(target -> sendWebform(
                        "sales-status",
                        target.getConversionStatusCode(),
                        target.getCustomerId()
                ))
                .toList();
    }

    /**
     * 웹폼 회수 처리
     */
    @Override
    @Transactional
    public void collectWebform(String uuidToken) {

        WebformIssuanceTargetResponse target =
                webformMapper.findIssuanceTargetByUuidToken(uuidToken);

        if (target == null) {
            throw new IllegalArgumentException("유효하지 않은 웹폼 UUID 토큰입니다.");
        }

        int updatedIssuanceCount =
                webformMapper.updateWebformCollected(uuidToken);

        if (updatedIssuanceCount == 0) {
            throw new IllegalStateException("웹폼 회수 처리에 실패했습니다.");
        }

        if ("01".equals(target.getConversionStatusCode())) {

            int updatedCustomerCount =
                    webformMapper.updatePotentialCustomerConsultStatus(
                            target.getCustomerId()
                    );

            if (updatedCustomerCount == 0) {
                throw new IllegalStateException("잠재고객 상담 상태 변경에 실패했습니다.");

            }
        }
    }

    /**
     * 고객 존재 여부 검증
     */
    private void validateCustomerExists(
            Long salesUserId,
            String conversionStatusCode,
            Long customerId
    ) {

        boolean exists;

        if ("01".equals(conversionStatusCode)) {

            exists = webformMapper.existsPotentialCustomerByIdAndSalesUserId(
                    salesUserId,
                    customerId
            );
        } else if ("02".equals(conversionStatusCode)) {

            exists = webformMapper.existsIntegratedCustomerByIdAndSalesUserId(
                    salesUserId,
                    customerId
            );
        } else {
            throw new IllegalArgumentException(
                    "유효하지 않은 고객 구분 코드입니다."
            );
        }

        if (!exists) {
            throw new IllegalArgumentException(
                    "존재하지 않거나 담당자가 아닌 고객입니다."
            );
        }
    }

    /**
     * 대시보드 웹폼 발송 대상 검증
     */
    private void validateDashboardSendTarget(
            Long salesUserId,
            String conversionStatusCode,
            Long customerId
    ) {

        if (!"01".equals(conversionStatusCode)) {
            throw new IllegalArgumentException(
                    "대시보드에서는 잠재고객만 발송할 수 있습니다."
            );
        }

        boolean isTarget =
                webformMapper.existsDashboardWebformTarget(
                        salesUserId,
                        customerId
                );

        if (!isTarget) {
            throw new IllegalArgumentException(
                    "오늘 연락 고객 대상이 아닙니다."
            );
        }
    }
}
