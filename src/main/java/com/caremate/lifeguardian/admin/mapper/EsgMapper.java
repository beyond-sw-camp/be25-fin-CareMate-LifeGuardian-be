package com.caremate.lifeguardian.admin.mapper;

import com.caremate.lifeguardian.admin.domain.InfraEsgCumulativeMetric;
import com.caremate.lifeguardian.admin.domain.InfraPowerHourlyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EsgMapper {

    //  ESG 누적 성과 지표 조회
    InfraEsgCumulativeMetric selectCumulativeMetric();

    // 특정 일자의 시간대별 인프라 부하 로그 전체 조회
    List<InfraPowerHourlyLog> selectHourlyPowerLogs(@Param("logDate") String logDate);
}
