package com.caremate.lifeguardian.report.mapper;

import com.caremate.lifeguardian.report.dto.internal.ReportSendAuditLogDto;
import com.caremate.lifeguardian.report.dto.internal.ReportSendTargetDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportSendMapper {

    // 개별 발송 최신 리포트 조회 및 접근 권한 검증
    ReportSendTargetDto selectReportSendTarget(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode,
            @Param("currentUserId") Long currentUserId
    );

    // 일괄 발송 대상 조회
    List<ReportSendTargetDto> selectBulkReportSendTargets(
            @Param("currentUserId") Long currentUserId,
            @Param("reportIds") List<Long> reportIds
    );

    // 리포트 발송 상태, 일시 갱신
    int updateReportSentStatus(
            @Param("reportId") Long reportId,
            @Param("sentAt") LocalDateTime sentAt
    );

    // 리포트 -> 감사 로그 저장
    int insertReportSendAuditLog(ReportSendAuditLogDto auditLog);

}
