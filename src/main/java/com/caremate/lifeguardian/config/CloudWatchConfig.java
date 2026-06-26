package com.caremate.lifeguardian.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

/**
 * AWS CloudWatch 클라이언트 빈을 Spring Container에 생성 및 주입한다.
 * 자격증명(accessKey, secretKey)이 설정되어 있지 않은 경우
 * DefaultCredentialsProvider를 통해 IAM Instance Profile 및 ECS Task Role을 자동으로 매핑한다.
 *
 * <p>로컬 환경(cloudwatch.enabled=false)에서는 AnonymousCredentialsProvider를 사용하여
 * EC2 IMDS 연결 시도로 인한 기동 지연을 방지한다.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EsgProperties.class)
public class CloudWatchConfig {

    @Bean
    public CloudWatchClient cloudWatchClient(
            EsgProperties esgProperties,
            @org.springframework.beans.factory.annotation.Value("${aws.cloudwatch.access-key:}") String accessKey,
            @org.springframework.beans.factory.annotation.Value("${aws.cloudwatch.secret-key:}") String secretKey,
            @org.springframework.beans.factory.annotation.Value("${aws.cloudwatch.region:ap-northeast-2}") String region
    ) {
        // cloudwatch.enabled=false(로컬) 이면 더미 자격증명으로 빈만 생성.
        // DefaultCredentialsProvider는 EC2 IMDS 연결을 시도해 서버 기동 지연을 유발할 수 있으므로
        // 실제 API 호출이 절대 발생하지 않는 경우에는 AnonymousCredentialsProvider를 사용한다.
        if (!esgProperties.getCloudwatch().isEnabled()) {
            log.info("CloudWatch is disabled. Creating a no-op CloudWatchClient (credentials will not be resolved).");
            return CloudWatchClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .build();
        }

        AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider(accessKey, secretKey);
        log.info("CloudWatchClient initialized with region={}", region);
        return CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private AwsCredentialsProvider resolveCredentialsProvider(String accessKey, String secretKey) {
        boolean hasAccessKey = StringUtils.hasText(accessKey);
        boolean hasSecretKey = StringUtils.hasText(secretKey);

        if (hasAccessKey && hasSecretKey) {
            log.info("CloudWatchClient using static credentials (accessKey configured).");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        }

        // IAM Instance Profile / ECS Task Role 등 환경에서 자동으로 자격증명을 탐색
        log.info("CloudWatchClient using DefaultCredentialsProvider (IAM role / instance profile).");
        return DefaultCredentialsProvider.create();
    }
}
