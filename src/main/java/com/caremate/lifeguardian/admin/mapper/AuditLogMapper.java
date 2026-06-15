package com.caremate.lifeguardian.admin.mapper;

import com.caremate.lifeguardian.admin.dto.request.AuditLogSearchRequest;
import com.caremate.lifeguardian.admin.dto.response.AuditLogInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuditLogMapper {

    //감사 로그 목록 페이징 및 조건 필터 조회
    List<AuditLogInfo> selectAuditLogs(@Param("request") AuditLogSearchRequest request);

    //감사 로그 총 개수 조회
    long countAuditLogs(@Param("request") AuditLogSearchRequest request);
}
