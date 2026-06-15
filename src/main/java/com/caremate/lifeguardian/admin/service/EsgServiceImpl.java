package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.domain.InfraEsgCumulativeMetric;
import com.caremate.lifeguardian.admin.domain.InfraPowerHourlyLog;
import com.caremate.lifeguardian.admin.dto.response.EnvironmentalScoresResponse;
import com.caremate.lifeguardian.admin.dto.response.PeakCutProfileResponse;
import com.caremate.lifeguardian.admin.mapper.EsgMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EsgServiceImpl implements EsgService {

    private final EsgMapper esgMapper;

    // 환경(E) 지표 누적 스코어보드 조회
    @Override
    public EnvironmentalScoresResponse getEnvironmentalScores() {
        log.info("환경(E) 누적 스코어보드 데이터 조회 시작");

        // DDL 설계상 ID=1인 단일행만 유지하도록 fix되어 있음
        InfraEsgCumulativeMetric metric = esgMapper.selectCumulativeMetric();

        if (metric == null) {
            log.warn("환경(E) 누적 스코어보드 데이터가 비어있습니다. 기본값으로 Fallback 처리합니다.");
            return EnvironmentalScoresResponse.builder()
                    .totalSavedCarbonKg(0.0)
                    .totalSavedCostKrw(0L)
                    .build();
        }

        // double 타입 null 방어 및 콤마 등 포맷 처리는 프론트엔드에 위임
        double carbonKg = metric.getTotalSavedCarbonKg() != null ? metric.getTotalSavedCarbonKg() : 0.0;
        long costKrw = metric.getTotalSavedCostKrw() != null ? metric.getTotalSavedCostKrw() : 0L;

        log.info("환경(E) 누적 스코어보드 데이터 조회 성공 - 탄소 절감량: {} kg, 비용 절감액: {} 원", carbonKg, costKrw);

        return EnvironmentalScoresResponse.builder()
                .totalSavedCarbonKg(carbonKg)
                .totalSavedCostKrw(costKrw)
                .build();
    }

    // 24시간 인프라 부하 및 피크 컷 차트 조회
    @Override
    public PeakCutProfileResponse getPeakCutProfile(String targetDate) {
        log.info("24시간 인프라 부하 및 피크 컷 차트 데이터 조회 시작 - targetDate: {}", targetDate);

        // 1. targetDate 기본값 설정 (D-1)
        if (targetDate == null || targetDate.trim().isEmpty()) {
            targetDate = LocalDate.now().minusDays(1).toString();
            log.info("조회 기준 날짜가 입력되지 않아 기본값(어제: {})으로 설정되었습니다.", targetDate);
        }

        // 2. DB 조회
        List<InfraPowerHourlyLog> logs = esgMapper.selectHourlyPowerLogs(targetDate);

        // 3. 빠른 탐색을 위해 logHour를 키로 하는 Map으로 변환
        Map<Integer, InfraPowerHourlyLog> logMap = logs.stream()
                .collect(Collectors.toMap(InfraPowerHourlyLog::getLogHour, Function.identity()));

        // 4. 00시부터 23시까지 Zero-Padding을 적용한 24개 리스트 구성
        String finalTargetDate = targetDate;
        List<PeakCutProfileResponse.HourlyProfileDto> hourlyProfiles = IntStream.range(0, 24)
                .mapToObj(hour -> {
                    String hourString = String.format("%02d:00", hour);
                    InfraPowerHourlyLog logEntity = logMap.get(hour);

                    double traditionalLoad = 0.0;
                    double optimizedLoad = 0.0;

                    if (logEntity != null) {
                        traditionalLoad = logEntity.getTraditionalEstimatedCpuUtil() != null 
                                ? logEntity.getTraditionalEstimatedCpuUtil() : 0.0;
                        optimizedLoad = logEntity.getOptimizedActualCpuUtil() != null 
                                ? logEntity.getOptimizedActualCpuUtil() : 0.0;
                    }

                    return PeakCutProfileResponse.HourlyProfileDto.builder()
                            .hour(hourString)
                            .traditionalLoad(traditionalLoad)
                            .optimizedLoad(optimizedLoad)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("24시간 인프라 부하 및 피크 컷 차트 데이터 조회 완료 - targetDate: {}, 데이터 크기: {}", finalTargetDate, hourlyProfiles.size());

        return PeakCutProfileResponse.builder()
                .targetDate(finalTargetDate)
                .hourlyProfiles(hourlyProfiles)
                .build();
    }
}
