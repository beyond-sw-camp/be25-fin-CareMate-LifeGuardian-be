package com.caremate.lifeguardian.esg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ESG 데이터 누적 배치를 매일 새벽 5시 30분에 자동 실행하는 스케줄러.
 * app.esg.batch.enabled=true 일 때만 활성화된다.
 */
@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(
        prefix = "app.esg.batch",
        name = "enabled",
        havingValue = "true"
)
public class EsgBatchScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;

    @Qualifier(EsgBatchConfiguration.JOB_NAME)
    private final Job esgDataAccumulationJob;

    public EsgBatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier(EsgBatchConfiguration.JOB_NAME) Job esgDataAccumulationJob
    ) {
        this.jobLauncher = jobLauncher;
        this.esgDataAccumulationJob = esgDataAccumulationJob;
    }

    /**
     * 매일 새벽 5시 30분 (Asia/Seoul 기준)에 ESG 배치를 실행한다.
     * cron / zone은 application.yml의 app.esg.batch 설정을 따른다.
     */
    @Scheduled(
            cron = "${app.esg.batch.cron:0 30 5 * * *}",
            zone = "${app.esg.batch.zone:Asia/Seoul}"
    )
    public void runEsgDataAccumulationJob() {
        LocalDateTime startedAt = LocalDateTime.now(SEOUL_ZONE);
        LocalDate targetDate = LocalDate.now(SEOUL_ZONE).minusDays(1);

        JobParameters parameters = new JobParametersBuilder()
                .addString("targetDate", targetDate.toString())
                .addString("startedAt", startedAt.toString())
                .toJobParameters();

        log.info("ESG batch scheduler triggered. targetDate={}, startedAt={}", targetDate, startedAt);

        try {
            jobLauncher.run(esgDataAccumulationJob, parameters);
        } catch (Exception e) {
            log.error("ESG batch failed to start. startedAt={}", startedAt, e);
        }
    }
}
