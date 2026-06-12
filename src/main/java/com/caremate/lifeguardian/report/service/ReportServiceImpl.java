package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.report.dto.internal.ActionItemInsertDto;
import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
- 리포트 생성 흐름 관리
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<ReportCreateResultDto> createReports(List<ReportTargetDto> targets) {
        List<ReportCreateResultDto> results = new ArrayList<>();

        for (ReportTargetDto target : targets) {
            try {
                ReportCreateResultDto result = transactionTemplate.execute(status ->
                        createReport(target)
                );
                results.add(result);
            } catch (Exception e) {
                results.add(ReportCreateResultDto.fail(target, e.getMessage()));
            }
        }

        return results;
    }

    private ReportCreateResultDto createReport(ReportTargetDto target) {
        validateTarget(target);

        ActionItemInsertDto actionItem = createActionItem(target);
        int insertedActionItemCount = reportMapper.insertActionItem(actionItem);

        if (insertedActionItemCount != 1 || actionItem.getId() == null) {
            throw new BaseException(500, "액션아이템 생성에 실패했습니다.");
        }

        CustomerReportInsertDto report = createCustomerReport(target, actionItem.getId());
        int insertedReportCount = reportMapper.insertCustomerReport(report);

        if (insertedReportCount != 1 || report.getId() == null) {
            throw new BaseException(500, "리포트 생성에 실패했습니다.");
        }

        return ReportCreateResultDto.success(target, actionItem.getId(), report);
    }

    private void validateTarget(ReportTargetDto target) {
        if ("04".equals(target.getReportTypeCode())
                && target.getWebFormId() == null) {
            throw new BaseException(400, "성장 리포트는 웹폼 응답이 필요합니다.");
        }
    }

    private ActionItemInsertDto createActionItem(ReportTargetDto target) {
        String triggerTypeCode = resolveTriggerTypeCode(target);

        return ActionItemInsertDto.builder()
                .currentUserId(target.getCurrentUserId())
                .customerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .triggerTypeCode(triggerTypeCode)
                .priorityScore(resolvePriorityScore(triggerTypeCode))
                .targetDate(LocalDate.now())
                .build();
    }

    private String resolveTriggerTypeCode(ReportTargetDto target) {
        return target.getWebFormId() != null ? "01" : "03";
    }

    private Integer resolvePriorityScore(String triggerTypeCode) {
        return switch (triggerTypeCode) {
            case "01" -> 80; // 웹폼 제출
            case "03" -> 40; // 미제출
            default -> 0;
        };
    }

    private CustomerReportInsertDto createCustomerReport(
            ReportTargetDto target,
            Long actionItemId
    ) {
        return CustomerReportInsertDto.builder()
                .customerId(target.getCustomerId())
                .conversionStatusCode(target.getConversionStatusCode())
                .actionItemId(actionItemId)
                .reportTypeCode(target.getReportTypeCode())
                .webformResponseId(target.getWebFormId())
                .reportUrl("TEMP_LOCAL_REPORT_URL")
                .sendStatusCode("01") // 발송대기
                .build();
    }
}
