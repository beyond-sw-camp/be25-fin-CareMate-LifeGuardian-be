package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.request.AuditLogSearchRequest;
import com.caremate.lifeguardian.admin.dto.response.AuditLogInfo;
import com.caremate.lifeguardian.admin.dto.response.AuditLogResponse;
import com.caremate.lifeguardian.admin.mapper.AuditLogMapper;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.security.SecurityUtil;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.mapper.SalesUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final SalesUserMapper salesUserMapper;

    @Override
    public AuditLogResponse getAuditLogs(AuditLogSearchRequest request) {
        // 1. ADMIN 권한 검증 (role_code = '01')
        Long currentUserId = SecurityUtil.getCurrentUserId();
        SalesUser currentUser = salesUserMapper.findById(currentUserId);
        if (currentUser == null || !"01".equals(currentUser.getRoleCode())) {
            throw new BaseException(403, "감사 로그를 조회할 권한이 없습니다.");
        }

        // 2. 날짜 기본값 및 유효성 검사
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();

        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().toString();
        } else {
            validateDateFormat(startDate);
        }

        if (endDate == null || endDate.isEmpty()) {
            endDate = LocalDate.now().toString();
        } else {
            validateDateFormat(endDate);
        }

        // 3. 날짜 역전 현상 검증
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                throw new BaseException(400, "시작일이 종료일보다 늦을 수 없습니다.");
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(400, "올바른 날짜 형식이 아닙니다.");
        }

        // 4. 새로운 요청 DTO 빌드
        AuditLogSearchRequest validatedRequest = AuditLogSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .actionTypeCode(request.getActionTypeCode())
                .page(request.getPage())
                .size(request.getSize())
                .build();

        // 5. 조회 수행
        long totalElements = auditLogMapper.countAuditLogs(validatedRequest);
        int size = validatedRequest.getSafeSize();
        int totalPages = (totalElements == 0) ? 0 : (int) Math.ceil((double) totalElements / size);
        List<AuditLogInfo> content = auditLogMapper.selectAuditLogs(validatedRequest);

        return AuditLogResponse.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .content(content)
                .build();
    }

    private void validateDateFormat(String date) {
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new BaseException(400, "올바른 날짜 형식(YYYY-MM-DD)이 아닙니다.");
        }
        try {
            LocalDate.parse(date);
        } catch (Exception e) {
            throw new BaseException(400, "존재하지 않는 날짜입니다.");
        }
    }
}
