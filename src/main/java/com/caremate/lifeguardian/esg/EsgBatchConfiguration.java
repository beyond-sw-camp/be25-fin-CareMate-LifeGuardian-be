package com.caremate.lifeguardian.esg;

import com.caremate.lifeguardian.admin.domain.InfraPowerHourlyLog;
import com.caremate.lifeguardian.esg.mapper.EsgSchedularMapper;
import com.caremate.lifeguardian.esg.service.CloudWatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * ESG 데이터 누적 배치 Job을 구성한다.
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>CloudWatchService로 전날 시간대별 실측 CPU 사용률(24건) 확보 (실패 시 Fallback)</li>
 *   <li>전력 소모 공식 + PUE 지수로 실제 시간당 전력 소모량(kWh) 산출</li>
 *   <li>피크컷 미적용 시 가상 부하율(traditional) 역계산</li>
 *   <li>탄소 배출 계수 + 한전 요금률로 전력/탄소/비용 절감량 산출</li>
 *   <li>delete → insert로 시간별 로그 갱신, 누적 테이블에 ON DUPLICATE KEY UPDATE 반영</li>
 * </ol>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EsgBatchConfiguration {

    public static final String JOB_NAME = "esgDataAccumulationJob";

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    // ── 전력 소모 상수 ──────────────────────────────────────────
    /** 서버 유휴(0%) 기저 전력 (kW) */
    private static final double BASE_POWER_KW = 8.0;
    /** 서버 100% 부하 시 최대 전력 (kW) */
    private static final double MAX_POWER_KW = 35.0;

    // ── PUE(Power Usage Effectiveness) 지수 ─────────────────────
    /** 주간(9~18시): 냉방 부하 증가 */
    private static final double PUE_DAYTIME = 1.25;
    /** 야간(22~6시): 냉방 절감 효과 */
    private static final double PUE_NIGHTTIME = 1.00;
    /** 기타 시간대 */
    private static final double PUE_DEFAULT = 1.15;

    // ── 탄소 배출 계수 (kgCO₂/kWh) ──────────────────────────────
    private static final double CARBON_FACTOR_DAYTIME = 0.4781;
    private static final double CARBON_FACTOR_NIGHTTIME = 0.4240;

    // ── 한전 전력 요금 (원/kWh) ──────────────────────────────────
    private static final double TARIFF_DAYTIME = 150.0;
    private static final double TARIFF_NIGHTTIME = 65.0;

    // ── 피크컷 야간 배치 추가 부하 추정치 ────────────────────────
    /** 피크컷 적용으로 야간에 옮겨진 배치 작업 부하율 추정값 (%) */
    private static final double BATCH_OFFLOAD_CPU_PERCENT = 18.0;

    private final EsgSchedularMapper esgMapper;
    private final CloudWatchService cloudWatchService;

    @Bean
    public Job esgDataAccumulationJob(JobRepository jobRepository, Step esgDataAccumulationStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(esgDataAccumulationStep)
                .build();
    }

    @Bean
    public Step esgDataAccumulationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("esgDataAccumulationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    LocalDate targetDate = LocalDate.now(SEOUL_ZONE).minusDays(1);
                    String targetDateStr = targetDate.toString(); // "yyyy-MM-dd"
                    log.info("ESG batch started. targetDate={}", targetDateStr);

                    // ── STEP 1: CloudWatch에서 시간별 실측 CPU 사용률 확보 ──
                    List<Double> actualCpuList = cloudWatchService.getHourlyCpuUtilization(targetDate);
                    log.info("CPU utilization data acquired: {} entries", actualCpuList.size());

                    // ── STEP 2~4: 시간대별 계산 ──────────────────────────
                    List<InfraPowerHourlyLog> logs = new ArrayList<>(24);
                    double totalSavedPower = 0.0;
                    double totalSavedCarbon = 0.0;
                    long totalSavedCost = 0L;

                    for (int hour = 0; hour < 24; hour++) {
                        double actualCpu = actualCpuList.get(hour); // 실측 CPU(%)

                        // STEP 2: 실제 시간당 전력 소모량 계산
                        double pue = resolvePue(hour);
                        double actualPowerKw = calcPowerKw(actualCpu) * pue;

                        // STEP 3: 피크컷 미적용 시 가상 부하율(traditional) 역산
                        // 주간(9~18시) 작업을 야간에 배치로 옮겼다고 가정 → 피크컷 없었다면 주간 CPU가 더 높았을 것
                        double traditionalCpu = resolveTraditionalCpu(hour, actualCpu);
                        double traditionalPowerKw = calcPowerKw(traditionalCpu) * pue;

                        // STEP 4: 탄소/비용 절감량 산출
                        double carbonFactor = resolveCarbonFactor(hour);
                        double tariff = resolveTariff(hour);

                        double savedPowerKwh = traditionalPowerKw - actualPowerKw; // 절감 전력(kWh)
                        double savedCarbonKg = savedPowerKwh * carbonFactor;       // 절감 탄소(kg)
                        long savedCostKrw = (long) (savedPowerKwh * tariff);       // 절감 비용(원)

                        // 절감량이 음수가 되지 않도록 보정 (야간 배치 가동으로 실제 전력이 더 높을 수 있음)
                        savedPowerKwh = Math.max(0, savedPowerKwh);
                        savedCarbonKg = Math.max(0, savedCarbonKg);
                        savedCostKrw = Math.max(0, savedCostKrw);

                        totalSavedPower += savedPowerKwh;
                        totalSavedCarbon += savedCarbonKg;
                        totalSavedCost += savedCostKrw;

                        logs.add(InfraPowerHourlyLog.builder()
                                .logDate(targetDate)
                                .logHour(hour)
                                .traditionalEstimatedCpuUtil(traditionalCpu)
                                .optimizedActualCpuUtil(actualCpu)
                                .powerConsumptionKw(actualPowerKw)
                                .build());
                    }

                    log.info("Calculation done. savedPower={}kWh, savedCarbon={}kg, savedCost={}KRW",
                            String.format("%.4f", totalSavedPower),
                            String.format("%.4f", totalSavedCarbon),
                            totalSavedCost);

                    // ── STEP 5: DB 영속화 (멱등성 확보: delete → insert) ──
                    esgMapper.deleteHourlyPowerLogs(targetDateStr);
                    esgMapper.insertHourlyPowerLogs(logs);
                    esgMapper.upsertCumulativeMetric(totalSavedCarbon, totalSavedPower, totalSavedCost);

                    log.info("ESG batch completed successfully. targetDate={}", targetDateStr);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 시간대에 따른 PUE 지수를 반환한다.
     * 주간(9~18시): 1.25 / 야간(22~23, 0~6시): 1.00 / 기타: 1.15
     */
    private double resolvePue(int hour) {
        if (hour >= 9 && hour < 18) return PUE_DAYTIME;
        if (hour >= 22 || hour < 6) return PUE_NIGHTTIME;
        return PUE_DEFAULT;
    }

    /**
     * CPU 사용률(%)로부터 서버 전력 소모량(kW)을 선형 보간하여 계산한다.
     * 공식: BASE_POWER + (MAX_POWER - BASE_POWER) * (cpu / 100)
     */
    private double calcPowerKw(double cpuPercent) {
        return BASE_POWER_KW + (MAX_POWER_KW - BASE_POWER_KW) * (cpuPercent / 100.0);
    }

    /**
     * 피크컷 적용 전 가상 전통 부하율을 역계산한다.
     * 주간(9~18시)에는 야간으로 옮긴 배치 작업량(BATCH_OFFLOAD_CPU_PERCENT)을 더해 추정한다.
     */
    private double resolveTraditionalCpu(int hour, double actualCpu) {
        if (hour >= 9 && hour < 18) {
            return Math.min(100.0, actualCpu + BATCH_OFFLOAD_CPU_PERCENT);
        }
        return actualCpu;
    }

    /**
     * 시간대에 따른 그리드 탄소 배출 계수(kgCO₂/kWh)를 반환한다.
     */
    private double resolveCarbonFactor(int hour) {
        if (hour >= 9 && hour < 18) return CARBON_FACTOR_DAYTIME;
        return CARBON_FACTOR_NIGHTTIME;
    }

    /**
     * 시간대에 따른 한전 전력 요금(원/kWh)을 반환한다.
     */
    private double resolveTariff(int hour) {
        if (hour >= 9 && hour < 18) return TARIFF_DAYTIME;
        return TARIFF_NIGHTTIME;
    }
}
