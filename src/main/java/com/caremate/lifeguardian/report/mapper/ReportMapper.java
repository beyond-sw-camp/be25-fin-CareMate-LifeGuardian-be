package com.caremate.lifeguardian.report.mapper;

import com.caremate.lifeguardian.report.dto.internal.ActionItemInsertDto;
import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper {
    int insertActionItem(ActionItemInsertDto actionItem);

    int insertCustomerReport(CustomerReportInsertDto report);
}
