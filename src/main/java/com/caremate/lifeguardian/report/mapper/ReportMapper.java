package com.caremate.lifeguardian.report.mapper;
import com.caremate.lifeguardian.report.dto.internal.data.DiseaseRiskItemDto;
import com.caremate.lifeguardian.report.dto.internal.data.GrowthStandardDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportCustomerInfoDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportWebformDto;
import com.caremate.lifeguardian.report.dto.request.CustomerReportInsertDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    // 리포트 표시할 고객, 보호자 정보
    ReportCustomerInfoDto selectReportCustomerInfo(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode
    );

    // 키, 몸무게 웹폼 응답 조회
    ReportWebformDto selectReportWebform(@Param("webFormId") Long webFormId);

    // 성별, 월 연령별 백분위 성장 조회
    List<GrowthStandardDto> selectGrowthStandards(
            @Param("gender") String gender,
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge
    );

    // 질병 위험 통계 조회
    List<DiseaseRiskItemDto> selectDiseaseRisks(
            @Param("ageGroupCode") String ageGroupCode,
            @Param("gender") String gender,
            @Param("treatmentType") String treatmentType,
            @Param("limit") int limit
    );

    // 생성된 리포트 테이블 저장
    int insertCustomerReport(CustomerReportInsertDto report);

}
