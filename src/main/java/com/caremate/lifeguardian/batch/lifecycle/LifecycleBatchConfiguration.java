package com.caremate.lifeguardian.batch.lifecycle;

import com.caremate.lifeguardian.lifecycle.mapper.LifecycleBatchMapper;
import com.caremate.lifeguardian.lifecycle.dto.internal.LifecycleTargetDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LifecycleBatchConfiguration {

    public static final String JOB_NAME = "potentialCustomerGraduationJob";

    private final LifecycleBatchMapper lifecycleBatchMapper;

    @Bean
    public Job potentialCustomerGraduationJob(
            JobRepository jobRepository,
            Step potentialCustomerGraduationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(potentialCustomerGraduationStep)
                .build();
    }

    @Bean
    public Step potentialCustomerGraduationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<LifecycleTargetDto> lifecycleTargetReader,
            ItemWriter<LifecycleTargetDto> lifecycleTargetWriter
    ) {
        return new StepBuilder("potentialCustomerGraduationStep", jobRepository)
                .<LifecycleTargetDto, LifecycleTargetDto>chunk(100, transactionManager)
                .reader(lifecycleTargetReader)
                .writer(lifecycleTargetWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<LifecycleTargetDto> lifecycleTargetReader() {
        List<LifecycleTargetDto> targets = lifecycleBatchMapper.selectLifecycleTargets();
        log.info("Potential customer graduation batch targets selected: count={}", targets.size());
        return new ListItemReader<>(targets);
    }

    @Bean
    public ItemWriter<LifecycleTargetDto> lifecycleTargetWriter() {
        return chunk -> {
            int updatedCount = 0;
            for (LifecycleTargetDto lifecycleTarget : chunk) {
                updatedCount += lifecycleBatchMapper.graduatePotentialCustomer(lifecycleTarget);
            }
            log.info("Potential customer graduation batch chunk completed: updated={}", updatedCount);
        };
    }
}
