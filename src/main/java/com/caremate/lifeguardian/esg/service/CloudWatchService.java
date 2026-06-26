package com.caremate.lifeguardian.esg.service;

import com.caremate.lifeguardian.config.EsgProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * AWS CloudWatch에서 EC2 인스턴스의 시간당 CPU 사용률을 조회하는 서비스.
 * 연동이 비활성화되었거나 실패한 경우, 사전에 설계된 24시간 CPU 부하량 추이를 기반으로
 * Fallback 가상 데이터를 생성하여 안전하게 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudWatchService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    // 24시간 Fallback 시뮬레이션 CPU 부하율 (인덱스 = 시간, 단위: %)
    // 야간 배치 집중 구간(0~5시)은 낮게, 업무 피크(9~18시)는 높게 설계
    private static final double[] FALLBACK_CPU_PROFILE = {
            15.0, 12.0, 10.0, 8.0, 9.0, 20.0,   // 00~05시: 야간 배치 후 급감
            25.0, 35.0, 55.0, 65.0, 70.0, 72.0,  // 06~11시: 업무 시작 급증
            75.0, 78.0, 76.0, 72.0, 68.0, 60.0,  // 12~17시: 업무 피크 유지
            45.0, 35.0, 28.0, 22.0, 18.0, 14.0   // 18~23시: 업무 종료 후 감소
    };

    private final CloudWatchClient cloudWatchClient;
    private final EsgProperties esgProperties;

    /**
     * 지정된 날짜의 시간대별 CPU 사용률(0~23시) 24개를 순서대로 반환한다.
     * CloudWatch 연동 비활성화 또는 호출 실패 시 Fallback 가상 데이터를 반환한다.
     *
     * @param targetDate 조회 대상 날짜 (어제 날짜를 사용하는 것을 권장)
     * @return 0시부터 23시까지 시간 순으로 정렬된 CPU 사용률(%) 리스트 (크기 24 보장)
     */
    public List<Double> getHourlyCpuUtilization(LocalDate targetDate) {
        if (!esgProperties.getCloudwatch().isEnabled()) {
            log.info("CloudWatch is disabled (app.esg.cloudwatch.enabled=false). Using fallback simulation data for {}.", targetDate);
            return buildFallbackData();
        }

        try {
            return fetchFromCloudWatch(targetDate);
        } catch (Exception e) {
            log.warn("Failed to fetch CloudWatch metrics for {}. Falling back to simulation data. Cause: {}", targetDate, e.getMessage());
            return buildFallbackData();
        }
    }

    /**
     * CloudWatch API를 통해 시간당 평균 CPUUtilization(Period 3600초)을 조회한다.
     */
    private List<Double> fetchFromCloudWatch(LocalDate targetDate) {
        EsgProperties.Cloudwatch cw = esgProperties.getCloudwatch();

        Instant startTime = targetDate.atStartOfDay(SEOUL_ZONE).toInstant();
        Instant endTime = targetDate.plusDays(1).atStartOfDay(SEOUL_ZONE).toInstant();

        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace(cw.getNamespace())
                .metricName(cw.getMetricName())
                .dimensions(Dimension.builder()
                        .name(cw.getDimensionName())
                        .value(cw.getDimensionValue())
                        .build())
                .startTime(startTime)
                .endTime(endTime)
                .period(3600) // 1시간 단위
                .statistics(Statistic.AVERAGE)
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
        List<Datapoint> datapoints = new ArrayList<>(response.datapoints());

        // 시간 순 정렬 후 24개 슬롯에 매핑
        datapoints.sort(Comparator.comparing(Datapoint::timestamp));

        log.info("CloudWatch returned {} datapoints for {}.", datapoints.size(), targetDate);

        // 시간(0~23)별 슬롯 초기화 (데이터가 없는 시간대는 0으로 처리)
        double[] hourlySlots = new double[24];
        for (Datapoint dp : datapoints) {
            int hour = dp.timestamp().atZone(SEOUL_ZONE).getHour();
            if (hour >= 0 && hour < 24) {
                hourlySlots[hour] = dp.average();
            }
        }

        List<Double> result = new ArrayList<>(24);
        for (double v : hourlySlots) {
            result.add(v);
        }
        return result;
    }

    /**
     * Fallback용 가상 CPU 부하율 데이터를 반환한다. (크기 24 보장)
     */
    private List<Double> buildFallbackData() {
        List<Double> result = new ArrayList<>(24);
        for (double v : FALLBACK_CPU_PROFILE) {
            result.add(v);
        }
        return result;
    }
}
