package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CustomerBasicInfoResponse {

    private Long customerId;
    private String conversionStatusCode;
    private String reportUrl;
    private Alert alert;
    private List<Badge> badges;
    private Child child;
    private LifeCycle lifeCycle;
    private Guardian guardian;

    @Getter
    @Builder
    public static class Alert {
        private String title;
        private String description;
        private String level;
    }

    @Getter
    @Builder
    public static class Badge {
        private String code;
        private String name;
    }

    @Getter
    @Builder
    public static class Child {
        private String name;
        private String gender;
        private Integer age;
        private LocalDate birthDate;
        private CodeName consultStatus;
        private CodeName conversionStatus;
    }

    @Getter
    @Builder
    public static class LifeCycle {
        private String lifeStageCode;
        private String lifeStageName;
        private LocalDate insuranceAgeShiftDate;
    }

    @Getter
    @Builder
    public static class Guardian {
        private Long parentCustomerId;
        private String name;
        private CodeName relation;
        private String phone;
        private String address;
        private Integer age;
    }

    @Getter
    @Builder
    public static class CodeName {
        private String code;
        private String name;
    }
}
