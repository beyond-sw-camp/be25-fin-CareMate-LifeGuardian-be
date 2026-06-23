package com.caremate.lifeguardian.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ESG 배치 및 CloudWatch 연동 제어를 위한 설정 바인딩 클래스.
 * application.yml의 app.esg 프로퍼티 계층을 바인딩한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.esg")
public class EsgProperties {

    private Batch batch = new Batch();
    private Cloudwatch cloudwatch = new Cloudwatch();

    @Getter
    @Setter
    public static class Batch {
        /** 배치 자동 실행 스케줄러 활성화 여부 */
        private boolean enabled = false;
        /** 배치 실행 크론 주기 (기본값: 매일 새벽 5시 30분) */
        private String cron = "0 30 5 * * *";
        /** 배치 기준 시간대 */
        private String zone = "Asia/Seoul";
    }

    @Getter
    @Setter
    public static class Cloudwatch {
        /** CloudWatch 실측 데이터 조회 활성화 여부 */
        private boolean enabled = false;
        /** CloudWatch 네임스페이스 */
        private String namespace = "AWS/EC2";
        /** CPU 메트릭 이름 */
        private String metricName = "CPUUtilization";
        /** 디멘션 식별자 (예: "InstanceId") */
        private String dimensionName = "InstanceId";
        /** 디멘션 값 (예: "i-0123456789abcdef0") */
        private String dimensionValue = "";
    }
}
