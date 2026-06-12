package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;

import java.util.List;

public interface ReportService {
    List<ReportCreateResultDto> createReports(List<ReportTargetDto> targets);
}
