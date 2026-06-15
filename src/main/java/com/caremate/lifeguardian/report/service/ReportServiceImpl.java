package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.report.dto.internal.ReportSendTargetDto;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportBulkSendResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportSendResultDto;
import com.caremate.lifeguardian.report.mapper.ReportSendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리포트 생성 요청과 개별·일괄 발송 흐름을 관리
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String SEND_PENDING = "01";
    private static final String SEND_SUCCESS = "02";

    private final ReportTransactionService reportTransactionService;
    private final ReportSendMapper reportSendMapper;

    /**
     * 고객별 생성 작업을 독립 트랜잭션으로 실행해 한 건의 실패가 전체 요청을 중단 X
     */
    @Override
    public List<ReportCreateResultDto> createReports(List<ReportTargetDto> targets) {
        List<ReportCreateResultDto> results = new ArrayList<>();

        for (ReportTargetDto target : targets) {
            try {
                validateTarget(target);
                results.add(reportTransactionService.createReport(target));
            } catch (Exception e) {
                results.add(ReportCreateResultDto.fail(target, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 담당 고객의 최신 리포트를 확인하고 발송 상태와 감사 로그를 함께 저장
     */
    @Override
    public ReportSendResultDto sendReport(
            Long customerId,
            String conversionStatusCode,
            Long currentUserId,
            String ipAddress,
            String userAgent
    ) {
        // 고객 아이디 null, 음수일 시
        if (customerId == null || customerId < 1) {
            throw new BaseException(400, "잘못된 고객 요청입니다.");
        }

        ReportSendTargetDto target = reportSendMapper.selectReportSendTarget(
                customerId,
                conversionStatusCode,
                currentUserId
        );
        // 해당 고객 리포트 존재 X
        if (target == null) {
            throw new BaseException(404, "발송할 고객 리포트를 찾을 수 없습니다.");
        }
        validateSendableReport(target);

        LocalDateTime sentAt = LocalDateTime.now();
        boolean sent = reportTransactionService.sendReportAndWriteAudit(
                target, currentUserId, ipAddress, userAgent, sentAt);
        if (!sent) {
            throw new BaseException(500, "리포트 발송 상태 저장에 실패했습니다.");
        }

        return ReportSendResultDto.builder()
                .customerId(target.getCustomerId())
                .customerName(target.getCustomerName())
                .sendStatusCode(SEND_SUCCESS)
                .sendStatusName("발송성공")
                .sentAt(sentAt)
                .build();
    }

    /**
     * 오늘 연락 대상의 최신 리포트를 건별 트랜잭션으로 발송한다.
     */
    @Override
    public ReportBulkSendResultDto sendReportsInBulk(
            Long currentUserId,
            String ipAddress,
            String userAgent // 요청 보낸 브라우저, 기기 정보 담은 HTTP 헤더
    ) {
        List<ReportSendTargetDto> targets =
                reportSendMapper.selectBulkReportSendTargets(currentUserId);

        if (targets.isEmpty()) {
            throw new BaseException(404, "발송 가능한 고객 리포트가 없습니다.");
        }
        int successCount = 0;
        int failedCount = 0;
        LocalDateTime sentAt = LocalDateTime.now();

        for (ReportSendTargetDto target : targets) {
            try {
                validateSendableReport(target);
                boolean sent = reportTransactionService.sendReportAndWriteAudit(
                        target, currentUserId, ipAddress, userAgent, sentAt);
                if (sent) {
                    successCount++;
                } else {
                    failedCount++;
                }
            } catch (Exception e) {
                failedCount++;
            }
        }

        return ReportBulkSendResultDto.builder()
                .requestedCount(targets.size())
                .successCount(successCount)
                .skippedCount(0)
                .failedCount(failedCount)
                .sentAt(sentAt)
                .build();
    }

    private void validateSendableReport(ReportSendTargetDto target) {
        if (target.getReportUrl() == null || target.getReportUrl().isBlank()) {
            throw new BaseException(404, "발송할 리포트 파일을 찾을 수 없습니다.");
        }
        if (!SEND_PENDING.equals(target.getSendStatusCode())
                && !SEND_SUCCESS.equals(target.getSendStatusCode())
                && !"03".equals(target.getSendStatusCode())) {
            throw new BaseException(409, "발송할 수 없는 리포트 상태입니다.");
        }
    }

    private void validateTarget(ReportTargetDto target) {
        if ("01".equals(target.getReportTypeCode())
                && target.getWebFormId() == null) {
            throw new BaseException(400, "성장 리포트는 웹폼 응답이 필요합니다.");
        }
    }
}
