package com.caremate.lifeguardian.userdetail.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.userdetail.dto.response.CustomerBasicInfoRow;
import com.caremate.lifeguardian.userdetail.dto.response.CustomerBadgeRow;
import com.caremate.lifeguardian.userdetail.dto.response.CustomerBasicInfoResponse;
import com.caremate.lifeguardian.userdetail.mapper.CustomerDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerDetailServiceImpl implements CustomerDetailService {

    private final CustomerDetailMapper customerDetailMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerBasicInfoResponse getCustomerBasicInfo(
            Long customerId,
            String conversionStatusCode,
            Long currentUserId
    ) {
        validateCustomerId(customerId);
        validateConversionStatusCode(conversionStatusCode);

        if (!customerDetailMapper.existsCustomer(customerId, conversionStatusCode)) {
            throw new BaseException(404, "고객 정보를 찾을 수 없습니다.");
        }

        CustomerBasicInfoRow row =
                customerDetailMapper.selectCustomerBasicInfo(
                        customerId,
                        conversionStatusCode,
                        currentUserId
                );

        if (row == null) {
            throw new BaseException(403, "해당 고객에 접근할 권한이 없습니다.");
        }

        List<CustomerBasicInfoResponse.Badge> badges = new ArrayList<>();
        for (CustomerBadgeRow badge : customerDetailMapper.selectCustomerBadges(
                customerId,
                conversionStatusCode,
                currentUserId
        )) {
            badges.add(CustomerBasicInfoResponse.Badge.builder()
                    .code(badge.getCode())
                    .name(badge.getName())
                    .build());
        }
        badges.add(CustomerBasicInfoResponse.Badge.builder()
                .code("BASIC_INFO")
                .name("기본정보 제공고객")
                .build());

        return CustomerBasicInfoResponse.builder()
                .customerId(row.getCustomerId())
                .conversionStatusCode(row.getConversionStatusCode())
                .reportUrl(row.getReportUrl())
                .alert(createAlert(row.getInsuranceAgeShiftDate()))
                .badges(badges)
                .child(createChild(row))
                .lifeCycle(createLifeCycle(row))
                .guardian(createGuardian(row))
                .build();
    }

    private void validateCustomerId(Long customerId) {
        if (customerId == null || customerId < 1) {
            throw new BaseException(400, "유효하지 않은 고객 ID입니다.");
        }
    }

    private void validateConversionStatusCode(String conversionStatusCode) {
        if (!"01".equals(conversionStatusCode)
                && !"02".equals(conversionStatusCode)) {
            throw new BaseException(400, "유효하지 않은 고객 전환 상태 코드입니다.");
        }
    }

    private CustomerBasicInfoResponse.Alert createAlert(LocalDate shiftDate) {
        if (shiftDate == null) {
            return null;
        }

        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), shiftDate);
        if (remainingDays < 0 || remainingDays > 30) {
            return null;
        }

        String level = remainingDays <= 7 ? "URGENT" : "WARNING";
        return CustomerBasicInfoResponse.Alert.builder()
                .title("상령일 도래 - 상담 필요")
                .description("보험 나이 상령일까지 %d일 남았습니다. 고객에게 보장 점검을 안내하세요."
                        .formatted(remainingDays))
                .level(level)
                .build();
    }

    private CustomerBasicInfoResponse.Child createChild(CustomerBasicInfoRow row) {
        return CustomerBasicInfoResponse.Child.builder()
                .name(row.getChildName())
                .gender(toDisplayGender(row.getChildGender()))
                .age(row.getChildAge())
                .birthDate(row.getChildBirthDate())
                .consultStatus(codeName(
                        row.getConsultStatusCode(),
                        row.getConsultStatusName()
                ))
                .conversionStatus(codeName(
                        row.getConversionStatusCode(),
                        row.getConversionStatusName()
                ))
                .build();
    }

    private CustomerBasicInfoResponse.LifeCycle createLifeCycle(CustomerBasicInfoRow row) {
        return CustomerBasicInfoResponse.LifeCycle.builder()
                .lifeStageCode(row.getLifeStageCode())
                .lifeStageName(row.getLifeStageName())
                .insuranceAgeShiftDate(row.getInsuranceAgeShiftDate())
                .build();
    }

    private CustomerBasicInfoResponse.Guardian createGuardian(CustomerBasicInfoRow row) {
        return CustomerBasicInfoResponse.Guardian.builder()
                .parentCustomerId(row.getParentCustomerId())
                .name(row.getGuardianName())
                .relation(codeName(
                        row.getRelationshipCode(),
                        row.getRelationshipName()
                ))
                .phone(row.getGuardianPhone())
                .address(row.getGuardianAddress())
                .age(row.getGuardianAge())
                .build();
    }

    private CustomerBasicInfoResponse.CodeName codeName(String code, String name) {
        if (code == null && name == null) {
            return null;
        }
        return CustomerBasicInfoResponse.CodeName.builder()
                .code(code)
                .name(name)
                .build();
    }

    private String toDisplayGender(String gender) {
        if ("MALE".equalsIgnoreCase(gender)) {
            return "남";
        }
        if ("FEMALE".equalsIgnoreCase(gender)) {
            return "여";
        }
        return gender;
    }
}
