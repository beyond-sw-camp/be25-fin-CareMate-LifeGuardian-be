package com.caremate.lifeguardian.batch.lifecycle;

import com.caremate.lifeguardian.batch.lifecycle.mapper.LifecycleBatchMapper;
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

/*
 * Step 방식: Tasklet 방식
 * 만 21세 이상 졸업 전 잠재고객을 한 번에 졸업 처리
 * log.info()로 졸업 처리 건수를 서버 로그에 기록
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LifecycleBatchConfiguration {

    // Job 이름
    public static final String JOB_NAME = "potentialCustomerGraduationJob";

    // 졸업 처리 UPDATE 실행 Mapper
    private final LifecycleBatchMapper lifecycleBatchMapper;

    // 잠재고객 졸업 처리 Job 정의
    @Bean
    public Job potentialCustomerGraduationJob(
            JobRepository jobRepository,
            Step potentialCustomerGraduationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(potentialCustomerGraduationStep)
                .build();
    }

    // 만 21세 이상 잠재고객을 단일 UPDATE로 졸업 처리하는 Step 정의
    @Bean
    public Step potentialCustomerGraduationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("potentialCustomerGraduationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int updatedCount = lifecycleBatchMapper.graduatePotentialCustomers();
                    contribution.getStepExecution()
                            .getExecutionContext()
                            .putInt("updatedCount", updatedCount);
                    log.info("Potential customer graduation batch completed: updated={}", updatedCount);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
