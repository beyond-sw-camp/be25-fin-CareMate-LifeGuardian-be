package com.caremate.lifeguardian.batch.lifecycle;

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

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(
        prefix = "app.lifecycle.batch",
        name = "enabled",
        havingValue = "true"
)
public class LifecycleBatchScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;

    @Qualifier(LifecycleBatchConfiguration.JOB_NAME)
    private final Job potentialCustomerGraduationJob;

    public LifecycleBatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier(LifecycleBatchConfiguration.JOB_NAME) Job potentialCustomerGraduationJob
    ) {
        this.jobLauncher = jobLauncher;
        this.potentialCustomerGraduationJob = potentialCustomerGraduationJob;
    }

    @Scheduled(
            cron = "${app.lifecycle.batch.cron:0 * * * * *}",
            zone = "${app.lifecycle.batch.zone:Asia/Seoul}"
    )
    public void runPotentialCustomerGraduationJob() {
        LocalDateTime startedAt = LocalDateTime.now(SEOUL_ZONE);
        JobParameters parameters = new JobParametersBuilder()
                .addString("startedAt", startedAt.toString())
                .toJobParameters();

        try {
            jobLauncher.run(potentialCustomerGraduationJob, parameters);
        } catch (Exception e) {
            log.error("Potential customer graduation batch failed to start: startedAt={}", startedAt, e);
        }
    }
}
