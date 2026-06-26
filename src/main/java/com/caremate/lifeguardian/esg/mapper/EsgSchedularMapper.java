package com.caremate.lifeguardian.esg.mapper;

import com.caremate.lifeguardian.admin.domain.InfraEsgCumulativeMetric;
import com.caremate.lifeguardian.admin.domain.InfraPowerHourlyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ESG 관련 DB 쿼리를 담당하는 MyBatis Mapper.
 * 배치 적재 전용 메서드(delete / insert / upsert)를 포함한다.
 */
@Mapper
public interface EsgSchedularMapper {

    /**
     * ESG 누적 성과 지표 조회
     */
    InfraEsgCumulativeMetric selectCumulativeMetric();

    /**
     * 특정 일자의 시간대별 인프라 부하 로그 전체 조회
     */
    List<InfraPowerHourlyLog> selectHourlyPowerLogs(@Param("logDate") String logDate);

    /**
     * 배치 재수행 시 멱등성 확보를 위해 특정 날짜의 시간별 로그를 우선 삭제한다.
     */
    void deleteHourlyPowerLogs(@Param("logDate") String logDate);

    /**
     * 24시간 시간별 전력 분석 로그를 벌크 인서트한다.
     */
    void insertHourlyPowerLogs(@Param("logs") List<InfraPowerHourlyLog> logs);

    /**
     * 누적 ESG 메트릭(id=1)에 절감량을 더하기 방식으로 업데이트한다.
     * 레코드가 없으면 신규 삽입, 있으면 누적 가산한다. (ON DUPLICATE KEY UPDATE)
     */
    void upsertCumulativeMetric(
            @Param("carbon") double carbon,
            @Param("power") double power,
            @Param("cost") long cost
    );
}
