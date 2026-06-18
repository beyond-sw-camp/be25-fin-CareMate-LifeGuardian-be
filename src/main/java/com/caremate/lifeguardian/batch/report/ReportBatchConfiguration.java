package com.caremate.lifeguardian.batch.report;

import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
import com.caremate.lifeguardian.report.mapper.ReportBatchMapper;
import com.caremate.lifeguardian.report.service.ReportService;
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
import java.util.List;

/**
 * 리포트 생성 대상 조회와 고객별 생성 처리를 수행하는 Spring Batch Job을 구성
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportBatchConfiguration {

    public static final String JOB_NAME = "customerReportCreationJob";

    private final ReportBatchMapper reportBatchMapper;
    private final ReportService reportService;

    @Bean
    public Job customerReportCreationJob(
            JobRepository jobRepository,
            Step customerReportCreationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(customerReportCreationStep)
                .build();
    }

    @Bean
    public Step customerReportCreationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("customerReportCreationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Integer reportYear = resolveReportYear(
                            chunkContext.getStepContext().getJobParameters().get("reportYear")
                    );
                    List<ReportTargetDto> targets = reportBatchMapper.selectReportTargets(reportYear);
                    List<ReportCreateResultDto> results = reportService.createReports(targets);

                    long successCount = results.stream()
                            .filter(ReportCreateResultDto::isSuccess)
                            .count();
                    long failedCount = results.size() - successCount;

                    contribution.getStepExecution().getExecutionContext()
                            .putInt("targetCount", targets.size());
                    contribution.getStepExecution().getExecutionContext()
                            .putLong("successCount", successCount);
                    contribution.getStepExecution().getExecutionContext()
                            .putLong("failedCount", failedCount);

                    log.info(
                            "Customer report batch completed: year={}, targets={}, success={}, failed={}",
                            reportYear,
                            targets.size(),
                            successCount,
                            failedCount
                    );
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Integer resolveReportYear(Object reportYearParameter) {
        if (reportYearParameter == null) {
            return LocalDate.now().getYear();
        }
        return Integer.valueOf(reportYearParameter.toString());
    }
}
