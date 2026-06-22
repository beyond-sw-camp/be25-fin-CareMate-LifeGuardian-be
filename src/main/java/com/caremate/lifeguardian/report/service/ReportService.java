package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportBulkSendResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.dto.response.ReportSendResultDto;

import java.util.List;

/**
 * 고객 리포트 생성과 발송 기능 제공
 */
public interface ReportService {

    // 요청된 고객별 리포트를 생성하고 각 요청의 성공 또는 실패 결과를 반환
    List<ReportCreateResultDto> createReports(List<ReportTargetDto> targets);

     // 로그인 사용자가 담당하는 특정 고객의 최신 리포트를 발송 처리
    ReportSendResultDto sendReport(
            Long customerId,
            String conversionStatusCode,
            Long currentUserId,
            String ipAddress,
            String userAgent
    );


     // 로그인 사용자의 오늘 연락 대상 고객 리포트를 일괄 발송 처리
    ReportBulkSendResultDto sendReportsInBulk(
            Long currentUserId,
            String ipAddress,
            String userAgent,
            List<Long> reportIds
    );
}
