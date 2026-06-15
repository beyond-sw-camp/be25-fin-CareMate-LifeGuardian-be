package com.caremate.lifeguardian.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.report.storage")
/*
- 리포트 저장소 설정을 바인딩하는 설정 클래스
 */
public class ReportStorageProperties {

    private String bucket;
    private String region;
    private String keyPrefix = "reports";
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String publicBaseUrl;
    private boolean pathStyleAccess;
}
