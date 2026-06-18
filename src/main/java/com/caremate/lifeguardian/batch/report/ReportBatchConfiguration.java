package com.caremate.lifeguardian.batch.report;

import com.caremate.lifeguardian.batch.report.mapper.ReportBatchMapper;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.response.ReportCreateResultDto;
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

/*
 * Step 방식: Tasklet 방식 -> 추후 chunk 방식 리팩토링 예정
 *
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportBatchConfiguration {

    // JOB 이름
    public static final String JOB_NAME = "customerReportCreationJob";

    // 실행 Mapper
    private final ReportBatchMapper reportBatchMapper; // 리포트 생성 대상 고객 조회
    private final ReportService reportService; // 리포트 생성

    // Job 등록
    @Bean
    public Job customerReportCreationJob(
            JobRepository jobRepository,
            Step customerReportCreationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(customerReportCreationStep)
                .build();
    }

    // 리포트 생성 Step
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
