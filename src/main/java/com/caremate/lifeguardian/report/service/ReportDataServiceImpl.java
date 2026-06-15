package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import com.caremate.lifeguardian.report.dto.internal.data.DiseaseRiskItemDto;
import com.caremate.lifeguardian.report.dto.internal.data.GrowthStandardDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportCustomerInfoDto;
import com.caremate.lifeguardian.report.dto.internal.data.ReportWebformDto;
import com.caremate.lifeguardian.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 고객, 웹폼, 성장 기준과 질병 통계를 조회해 리포트 템플릿 변수를 구성
 */
@Service
@RequiredArgsConstructor
public class ReportDataServiceImpl {

    private static final String GROWTH_REPORT_TYPE = "01";

    private final ReportMapper reportMapper;
    private final GrowthChartService growthChartService;

    /**
     * 리포트 대상과 유형에 맞는 모든 데이터를 조회하고 템플릿 변수로 반환
     */
    public Map<String, Object> createTemplateVariables(ReportTargetDto target) {
        ReportCustomerInfoDto customer = reportMapper.selectReportCustomerInfo(
                target.getCustomerId(),
                target.getConversionStatusCode()
        );

        if (customer == null) {
            throw new BaseException(404, "리포트 대상 고객 정보를 찾을 수 없습니다.");
        }

        AgeGroup currentAgeGroup = AgeGroup.fromAge(customer.getChildAge());
        AgeGroup nextAgeGroup = currentAgeGroup.next();
        ReportWebformDto webform = target.getWebFormId() == null
                ? null
                : reportMapper.selectReportWebform(target.getWebFormId());

        List<DiseaseRiskItemDto> currentRisks = loadRisks(
                currentAgeGroup,
                customer.getChildGender(),
                3
        );
        List<DiseaseRiskItemDto> nextRisks = nextAgeGroup == null
                ? List.of()
                : loadRisks(nextAgeGroup, customer.getChildGender(), 2);

        List<GrowthStandardDto> growthStandards = List.of();
        String heightSummary = null;
        String weightSummary = null;
        String overallSummary = null;

        if (webform != null && GROWTH_REPORT_TYPE.equals(target.getReportTypeCode())) {
            int ageMonth = customer.getChildAgeMonth();
            int rangeStart = resolveGrowthRangeStart(ageMonth);
            int rangeEnd = resolveGrowthRangeEnd(ageMonth);
            growthStandards = reportMapper.selectGrowthStandards(
                    customer.getChildGender(),
                    rangeStart,
                    rangeEnd
            );
            ensureCurrentMonthIncluded(growthStandards, customer.getChildGender(), ageMonth);
            prepareGrowthChart(growthStandards, webform, ageMonth);
            heightSummary = createGrowthSummary(
                    "키", webform.getHeight(), growthStandards, ageMonth, true);
            weightSummary = createGrowthSummary(
                    "몸무게", webform.getWeight(), growthStandards, ageMonth, false);
            overallSummary = createOverallSummary(heightSummary, weightSummary);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("reportYear", target.getReportYear() == null
                ? LocalDate.now().getYear()
                : target.getReportYear());
        variables.put("generatedAt", LocalDate.now());
        variables.put("reportTitle", webform == null ? "질병 통계 리포트" : "성장 리포트");
        variables.put("hasWebform", webform != null);
        variables.put("customer", customer);
        variables.put("webform", webform);
        variables.put("growthStandards", growthStandards);
        variables.put("growthChartDataUri", growthStandards.isEmpty()
                ? null
                : growthChartService.createCombinedChart(growthStandards));
        variables.put("currentRisks", currentRisks);
        variables.put("nextRisks", nextRisks);
        variables.put("currentAgeGroupName", currentAgeGroup.displayName);
        variables.put("nextAgeGroupName", nextAgeGroup == null ? null : nextAgeGroup.displayName);
        variables.put("heightSummary", heightSummary);
        variables.put("weightSummary", weightSummary);
        variables.put("overallSummary", overallSummary);
        return variables;
    }

    /**
     * 연령대와 성별에 해당하는 주요 질병 위험을 지정 개수만큼 구성한다.
     */
    private List<DiseaseRiskItemDto> loadRisks(
            AgeGroup ageGroup,
            String gender,
            int limit
    ) {
        List<DiseaseRiskItemDto> risks = new ArrayList<>(
                reportMapper.selectDiseaseRisks(ageGroup.databaseCode, gender, Math.max(limit, 10))
        );

        return risks.stream()
                .limit(limit)
                .peek(item -> item.setDescription(createRiskDescription(ageGroup, item)))
                .toList();
    }

    private String createRiskDescription(
            AgeGroup ageGroup,
            DiseaseRiskItemDto item
    ) {
        StringBuilder description = new StringBuilder()
                .append(ageGroup.displayName)
                .append("에서 ")
                .append(item.getDiseaseName())
                .append(" 관련 ")
                .append(item.getTreatmentType())
                .append(" 진료가 많이 나타납니다.");

        if (item.getCategoryName() != null) {
            description.append(" ").append(item.getCategoryName()).append(" 점검이 필요할 수 있습니다.");
        }
        return description.toString();
    }

    /**
     * 현재 측정값을 동일 연령 성장 기준에 연결하고 그래프 표시값을 계산
     */
    private void prepareGrowthChart(
            List<GrowthStandardDto> standards,
            ReportWebformDto webform,
            int childAgeMonth
    ) {
        for (GrowthStandardDto standard : standards) {
            if (standard.getAgeMonth() == childAgeMonth) {
                standard.setChildHeight(webform.getHeight());
                standard.setChildWeight(webform.getWeight());
            }

            BigDecimal heightMax = max(standard.getHeightP95(), standard.getChildHeight())
                    .multiply(new BigDecimal("1.10"));
            BigDecimal weightMax = max(standard.getWeightP95(), standard.getChildWeight())
                    .multiply(new BigDecimal("1.10"));

            applyGrowthWidths(standard, heightMax, weightMax);
        }
    }

    private void applyGrowthWidths(
            GrowthStandardDto standard,
            BigDecimal heightMax,
            BigDecimal weightMax
    ) {
        standard.setHeightP5Width(toPercent(standard.getHeightP5(), heightMax));
        standard.setHeightP50Width(toPercent(standard.getHeightP50(), heightMax));
        standard.setHeightP95Width(toPercent(standard.getHeightP95(), heightMax));
        standard.setChildHeightWidth(toPercent(standard.getChildHeight(), heightMax));

        standard.setWeightP5Width(toPercent(standard.getWeightP5(), weightMax));
        standard.setWeightP50Width(toPercent(standard.getWeightP50(), weightMax));
        standard.setWeightP95Width(toPercent(standard.getWeightP95(), weightMax));
        standard.setChildWeightWidth(toPercent(standard.getChildWeight(), weightMax));
    }

    /**
     * 키 또는 몸무게를 백분위 기준과 비교해 사용자용 설명 문장을 만든다.
     */
    private String createGrowthSummary(
            String label,
            BigDecimal childValue,
            List<GrowthStandardDto> standards,
            int childAgeMonth,
            boolean height
    ) {
        GrowthStandardDto standard = standards.stream()
                .filter(item -> item.getAgeMonth() == childAgeMonth)
                .findFirst()
                .orElse(null);

        if (standard == null || childValue == null) {
            return label + "를 비교할 동일 연령·성별 성장 기준 데이터가 없습니다.";
        }

        BigDecimal p5 = height ? standard.getHeightP5() : standard.getWeightP5();
        BigDecimal p50 = height ? standard.getHeightP50() : standard.getWeightP50();
        BigDecimal p95 = height ? standard.getHeightP95() : standard.getWeightP95();

        if (!height) {
            if (childValue.compareTo(p5) < 0) {
                return "몸무게가 같은 성별·나이 또래 중 하위 5%보다 적게 나가는 편입니다.";
            }
            if (childValue.compareTo(p50) < 0) {
                return "몸무게가 같은 성별·나이 또래의 평균보다 적게 나가는 편입니다.";
            }
            if (childValue.compareTo(p95) <= 0) {
                return "몸무게가 같은 성별·나이 또래의 평균보다 많이 나가는 편입니다.";
            }
            return "몸무게가 같은 성별·나이 또래 중 상위 5%보다 많이 나가는 편입니다.";
        }

        if (childValue.compareTo(p5) < 0) {
            return "%s가 같은 성별·나이 또래 중 하위 5%%보다 작은 편입니다.".formatted(label);
        }
        if (childValue.compareTo(p50) < 0) {
            return "%s가 같은 성별·나이 또래의 평균보다 작은 편입니다.".formatted(label);
        }
        if (childValue.compareTo(p95) <= 0) {
            return "%s가 같은 성별·나이 또래의 평균보다 큰 편입니다.".formatted(label);
        }
        return "%s가 같은 성별·나이 또래 중 상위 5%%보다 큰 편입니다.".formatted(label);
    }

    /**
     * 조회 범위에 현재 월령 데이터가 빠진 경우 해당 월령을 추가한다.
     */
    private void ensureCurrentMonthIncluded(
            List<GrowthStandardDto> standards,
            String gender,
            int ageMonth
    ) {
        boolean included = standards.stream()
                .anyMatch(item -> item.getAgeMonth() == ageMonth);
        if (included) {
            return;
        }

        List<GrowthStandardDto> current = reportMapper.selectGrowthStandards(
                gender,
                ageMonth,
                ageMonth
        );
        if (!current.isEmpty()) {
            standards.add(current.get(0));
            standards.sort(java.util.Comparator.comparing(GrowthStandardDto::getAgeMonth));
        }
    }

    private int resolveGrowthRangeStart(int ageMonth) {
        if (ageMonth <= 36) {
            return 0;
        }
        return Math.max(0, ageMonth - 18);
    }

    private int resolveGrowthRangeEnd(int ageMonth) {
        if (ageMonth <= 36) {
            return 36;
        }
        return Math.min(227, ageMonth + 18);
    }

    private String createOverallSummary(String heightSummary, String weightSummary) {
        return heightSummary + " " + weightSummary
                + " 성장 수치는 진단 결과가 아니며, 또래 기준과 비교한 상담 참고 정보입니다.";
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        if (first == null) {
            return second == null ? BigDecimal.ONE : second;
        }
        return second == null ? first : first.max(second);
    }

    private int toPercent(BigDecimal value, BigDecimal max) {
        if (value == null || max == null || max.signum() == 0) {
            return 0;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(max, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private enum AgeGroup {
        AGE_01(0, 4, "AGE_01", "0~4세"),
        AGE_02(5, 9, "AGE_02", "5~9세"),
        AGE_03(10, 14, "AGE_03", "10~14세"),
        AGE_04(15, 19, "AGE_04", "15~19세");

        private final int minAge;
        private final int maxAge;
        private final String databaseCode;
        private final String displayName;

        AgeGroup(int minAge, int maxAge, String databaseCode, String displayName) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.databaseCode = databaseCode;
            this.displayName = displayName;
        }

        private static AgeGroup fromAge(int age) {
            for (AgeGroup group : values()) {
                if (age >= group.minAge && age <= group.maxAge) {
                    return group;
                }
            }
            return age < 0 ? AGE_01 : AGE_04;
        }

        private AgeGroup next() {
            int nextOrdinal = ordinal() + 1;
            return nextOrdinal < values().length ? values()[nextOrdinal] : null;
        }
    }
}
