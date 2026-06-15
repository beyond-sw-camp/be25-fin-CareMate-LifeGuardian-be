package com.caremate.lifeguardian.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(ReportStorageProperties.class)
public class S3Config {

    /*
    - S3 통신 S3Client 설정 -> Spring Bean 생성
     */
    @Bean
    public S3Client s3Client(ReportStorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .chunkedEncodingEnabled(false)
                        .build());

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(ReportStorageProperties properties) {
        boolean hasAccessKey = StringUtils.hasText(properties.getAccessKey());
        boolean hasSecretKey = StringUtils.hasText(properties.getSecretKey());

        // R2 키 있으면 해당 키 사용
        if (hasAccessKey != hasSecretKey) {
            throw new IllegalStateException(
                    "R2_ACCESS_KEY_ID와 R2_SECRET_ACCESS_KEY는 함께 설정해야 합니다."
            );
        }

        if (hasAccessKey) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    properties.getAccessKey(),
                    properties.getSecretKey()
            );
            return StaticCredentialsProvider.create(credentials);
        }

        return DefaultCredentialsProvider.create();
    }
}
