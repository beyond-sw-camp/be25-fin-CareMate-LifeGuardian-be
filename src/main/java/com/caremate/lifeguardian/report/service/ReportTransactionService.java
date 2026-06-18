package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.report.dto.internal.ActionItemInsertDto;
import com.caremate.lifeguardian.report.dto.internal.ReportSendAuditLogDto;
import com.caremate.lifeguardian.report.dto.internal.ReportSendTargetDto;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.batch.report.mapper.ReportBatchMapper;
import com.caremate.lifeguardian.report.mapper.ReportMapper;
import com.caremate.lifeguardian.report.mapper.ReportSendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 한 고객 단위의 리포트 생성과 발송 DB 작업에 트랜잭션 경계를 제공
 */
@Service
@RequiredArgsConstructor
public class ReportTransactionService {

    private static final String AUDIT_REPORT_SEND = "03";
    private static final String SEND_SUCCESS = "02";

    private final ReportMapper reportMapper;
    private final ReportBatchMapper reportBatchMapper;
    private final ReportSendMapper reportSendMapper;

    private final ReportDataServiceImpl reportDataService;
    private final ReportDocumentService reportDocumentService;
    private final ReportStorageServiceImpl reportStorageService;

    /**
     * 템플릿 데이터 구성부터 PDF 업로드와 DB 이력 생성을 한 고객 단위로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportCreateResultDto createReport(ReportTargetDto target) {
        Map<String, Object> variables = reportDataService.createTemplateVariables(target);
        byte[] pdfBytes = reportDocumentService.renderPdf("growth-report", variables);
        String reportUrl = reportStorageService.uploadPdf(pdfBytes, target.getCustomerId());

        ActionItemInsertDto actionItem = createActionItem(target);
        int insertedActionItemCount = reportBatchMapper.insertActionItem(actionItem);
        if (insertedActionItemCount != 1 || actionItem.getId() == null) {
            throw new BaseException(500, "액션아이템 생성에 실패했습니다.");
        }

        CustomerReportInsertDto report = createCustomerReport(target, actionItem.getId(), reportUrl);
        int insertedReportCount = reportMapper.insertCustomerReport(report);
        if (insertedReportCount != 1 || report.getId() == null) {
            throw new BaseException(500, "리포트 생성에 실패했습니다.");
        }

        return ReportCreateResultDto.success(target, actionItem.getId(), report);
    }

    /**
     * 발송 상태 변경과 감사 로그 저장을 한 트랜잭션에서 처리한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean sendReportAndWriteAudit(
            ReportSendTargetDto target,
            Long currentUserId,
            String ipAddress,
            String userAgent,
            LocalDateTime sentAt
    ) {
        int updatedCount = reportSendMapper.updateReportSentStatus(target.getReportId(), sentAt);
        if (updatedCount != 1) {
            return false;
        }

        ReportSendAuditLogDto auditLog = ReportSendAuditLogDto.builder()
                .currentUserId(currentUserId)
                .targetCustomerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .actionTypeCode(AUDIT_REPORT_SEND)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .reason(resolveSendAuditReason(target))
                .build();

        int auditCount = reportSendMapper.insertReportSendAuditLog(auditLog);
        if (auditCount != 1) {
            throw new BaseException(500, "리포트 발송 감사 로그 저장에 실패했습니다.");
        }
        return true;
    }

    private ActionItemInsertDto createActionItem(ReportTargetDto target) {
        String triggerTypeCode = target.getWebFormId() != null ? "01" : "03";

        return ActionItemInsertDto.builder()
                .currentUserId(target.getCurrentUserId())
                .customerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .triggerTypeCode(triggerTypeCode)
                .priorityScore(resolvePriorityScore(triggerTypeCode))
                .targetDate(LocalDate.now())
                .build();
    }

    private Integer resolvePriorityScore(String triggerTypeCode) {
        return switch (triggerTypeCode) {
            case "01" -> 80;
            case "03" -> 40;
            default -> 0;
        };
    }

    private CustomerReportInsertDto createCustomerReport(
            ReportTargetDto target,
            Long actionItemId,
            String reportUrl
    ) {
        return CustomerReportInsertDto.builder()
                .customerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .actionItemId(actionItemId)
                .reportTypeCode(target.getReportTypeCode())
                .webformResponseId(target.getWebFormId())
                .reportUrl(reportUrl)
                .sendStatusCode("01")
                .build();
    }

    private String resolveSendAuditReason(ReportSendTargetDto target) {
        String sendType = switch (target.getSendStatusCode()) {
            case SEND_SUCCESS -> "재발송";
            case "03" -> "실패 후 재시도";
            default -> "최초 발송";
        };
        return "고객 리포트 " + sendType + ": reportId=" + target.getReportId();
    }
}
