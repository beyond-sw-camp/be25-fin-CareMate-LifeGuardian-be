package com.caremate.lifeguardian.batch.report;

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
 * 설정된 주기에 따라 고객 리포트 생성 Job을 실행.
 */
@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(
        prefix = "app.report.batch",
        name = "enabled",
        havingValue = "true"
)
public class ReportBatchScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;

    @Qualifier(ReportBatchConfiguration.JOB_NAME)
    private final Job customerReportCreationJob;

    public ReportBatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier(ReportBatchConfiguration.JOB_NAME) Job customerReportCreationJob
    ) {
        this.jobLauncher = jobLauncher;
        this.customerReportCreationJob = customerReportCreationJob;
    }

    // 기본 설정
    @Scheduled(
            cron = "${app.report.batch.cron:0 0 3 * * *}",
            zone = "${app.report.batch.zone:Asia/Seoul}"
    )
    public void runCustomerReportCreationJob() {
        LocalDateTime startedAt = LocalDateTime.now(SEOUL_ZONE);
        JobParameters parameters = new JobParametersBuilder()
                .addLong("reportYear", (long) LocalDate.now(SEOUL_ZONE).getYear())
                .addString("startedAt", startedAt.toString())
                .toJobParameters();

        try {
            jobLauncher.run(customerReportCreationJob, parameters);
        } catch (Exception e) {
            log.error("Customer report batch failed to start: startedAt={}", startedAt, e);
        }
    }
}
