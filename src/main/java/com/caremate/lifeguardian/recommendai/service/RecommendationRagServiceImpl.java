package com.caremate.lifeguardian.recommendai.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.recommendation.domain.InsurancePlan;
import com.caremate.lifeguardian.recommendation.domain.RecommendationLog;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.CustomerInfoDto;
import com.caremate.lifeguardian.recommendai.dto.CoverageDto;
import com.caremate.lifeguardian.recommendai.dto.response.RecommendAiResponse;
import com.caremate.lifeguardian.recommendai.dto.ScoreDetailDto;
import com.caremate.lifeguardian.recommendai.mapper.RecommendAiMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationRagServiceImpl implements RecommendationRagService {

    private final RecommendAiMapper recommendAiMapper;
    private final RagTextConverter ragTextConverter;
    private final RecommendAiClient recommendAiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RecommendAiResponse analyzeAndBuildPortfolio(Long customerId) {
        log.info("Processing AI RAG recommendation for customerId: {}", customerId);

        // 1. MariaDB: 웹폼 데이터 원천 조회 (고객 ID 기반 최신 웹폼 조회)
        WebformResponse webform = recommendAiMapper.findLatestWebformByCustomerId(customerId);
        if (webform == null) {
            throw new BaseException(404, "웹폼이 제출되지 않은 고객입니다.");
        }

        // 2. 고객 정보 조회 및 나이 계산
        CustomerInfoDto customer = recommendAiMapper.findCustomerInfo(customerId);
        if (customer == null) {
            throw new BaseException(500, "고객 정보가 존재하지 않습니다: " + customerId);
        }
        int childAge = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();

        // 3. 정형 코드를 한글 텍스트 컨텍스트로 변환
        String embeddingQueryText = ragTextConverter.convertToEmbeddingQuery(
                webform.getSelectedPriorityCategory(),
                webform.getHistoryJson(),
                webform.getActivityJson(),
                webform.getPastSurgeryOrHospitalization() ? 1 : 0
        );
        log.info("Generated RAG query text: {}", embeddingQueryText);

        // 4. Python Lambda: pgvector 기반 코사인 유사도 담보 명단 스캔
        List<String> rankedRiderNames = recommendAiClient.getRecommendedRiders(embeddingQueryText, childAge);
        log.info("Received recommended riders from AI server: {}", rankedRiderNames);

        // 5. 예산 한도 내 가설계 조립 처리 (Break 임계치 검증)
        int budgetCeiling = parseBudgetCode(webform.getDesiredBudgetCode());
        int currentTotalPremium = 0;
        int orderSeq = 1;

        List<CoverageDto> finalizedCoverages = new ArrayList<>();
        List<CoverageCandidateDto> matchedCoverages = new ArrayList<>();

        for (String riderName : rankedRiderNames) {
            CoverageCandidateDto coverage = recommendAiMapper.findCoverageByName(riderName);
            if (coverage == null) {
                continue;
            }

            // 예산 초과 시 루프 중단
            if (currentTotalPremium + coverage.getUnitPremium() > budgetCeiling) {
                log.info("Budget limit reached ({} > {}). Stopping selection.", currentTotalPremium + coverage.getUnitPremium(), budgetCeiling);
                break;
            }

            currentTotalPremium += coverage.getUnitPremium();
            matchedCoverages.add(coverage);

            finalizedCoverages.add(CoverageDto.builder()
                    .coverageId(coverage.getCoverageId())
                    .coverageName(coverage.getCoverageName())
                    .categoryCode(coverage.getCategoryCode())
                    .categoryName(ragTextConverter.getKoreanLabel(coverage.getCategoryCode()))
                    .unitPremium(coverage.getUnitPremium())
                    .selectedOrder(orderSeq++)
                    .coverageSummary(coverage.getCoverageName() + " 사고/질병 발생 시 약관에 명시된 가입금액을 전액 보장합니다.")
                    .exclusionReasons(List.of("피보험자의 고의적인 자해/상해 사고", "단순 치아 미용 목적의 치료 및 보철", "알코올 및 중독성 물질 흡입 상태에서의 사고"))
                    .build());
        }

        // 6. MariaDB 영속화
        // 6-1. 최종 보험 플랜 저장
        String planName = "AI RAG 맞춤 추천 " + ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory());
        InsurancePlan plan = InsurancePlan.builder()
                .webformResponseId(webform.getId())
                .planName(planName)
                .totalPremium(currentTotalPremium)
                .recommendationTypeCode("02") // 02: AI RAG 추천 구분 메타코드
                .scriptData("{\"engine\": \"FastAPI-pgvector\"}")
                .build();
        recommendAiMapper.insertInsurancePlan(plan);

        // 6-2. 플랜 조립 담보 세부 매핑 테이블 저장
        for (CoverageDto covDto : finalizedCoverages) {
            recommendAiMapper.insertPlanCoverage(
                    plan.getId(),
                    covDto.getCoverageId(),
                    covDto.getSelectedOrder(),
                    covDto.getUnitPremium()
            );
        }

        // 6-3. 추천 상담 로그 적재
        String budgetRangeName = parseBudgetRangeName(webform.getDesiredBudgetCode());
        String recommendReasonStr = String.format("%s 고객님은 웹폼 문맥 분석 결과 %s 한도 예산 범위 내에서 최우선 순위 보장인 '%s'를 중심으로 최적화 결합되었습니다.",
                customer.getName(),
                budgetRangeName,
                ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory()));

        String scriptContentText = String.format("고객님께서 가장 중점적으로 생각하신 '%s' 부장을 축으로, 건강 위험 문진 키워드들을 벡터 분석하여 월 %s 예산 내에서 가장 우선순위가 높은 핵심 담보를 조합해 플랜을 설계했습니다.",
                ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory()), budgetRangeName);

        String scriptDataJson = "{}";
        try {
            Map<String, Object> scriptDataMap = new LinkedHashMap<>();
            scriptDataMap.put("summary", String.format("%s 중심으로 월 %,d원 예산 내 핵심 담보를 추천했습니다.",
                    ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory()), currentTotalPremium));
            scriptDataMap.put("salesScript", scriptContentText);
            scriptDataJson = objectMapper.writeValueAsString(scriptDataMap);
        } catch (Exception e) {
            log.error("Failed to convert script data to JSON", e);
        }

        RecommendationLog recLog = RecommendationLog.builder()
                .potentialCustomerId(webform.getCustomerId())
                .targetInsuredType("01")
                .webformResponseId(webform.getId())
                .salesUserId(customer.getSalesUserId() != null ? customer.getSalesUserId() : 7L)
                .recommendedCategoryCode(webform.getSelectedPriorityCategory())
                .totalScore(0) // AI 추천 점수판 동기화 규칙 (기본값 0)
                .recommendReason(recommendReasonStr)
                .scriptData(scriptDataJson)
                .build();
        recommendAiMapper.insertRecommendationLog(recLog);

        // 7. DTO 매핑 및 리턴
        List<ScoreDetailDto> scoreDetails = new ArrayList<>();
        List<String> allPickedOptions = new ArrayList<>();
        allPickedOptions.addAll(ragTextConverter.parseJsonArray(webform.getHistoryJson()));
        allPickedOptions.addAll(ragTextConverter.parseJsonArray(webform.getActivityJson()));

        for (String optionCode : allPickedOptions) {
            scoreDetails.add(ScoreDetailDto.builder()
                    .questionKey("choice")
                    .selectedOptionValue(ragTextConverter.getKoreanLabel(optionCode))
                    .categoryCode(webform.getSelectedPriorityCategory())
                    .categoryName(ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory()))
                    .reasonMessage("선택하신 키워드가 AI 임베딩 벡터 공간 분석에서 랭킹 가중치에 수렴되었습니다.")
                    .build());
        }

        return RecommendAiResponse.builder()
                .customerId(webform.getCustomerId())
                .conversionStatusCode(webform.getConversionStatusCode())
                .webformResponseId(webform.getId())
                .recommendationId(recLog.getId())
                .insurancePlanId(plan.getId())
                .recommendationTypeCode("02")
                .recommendationTypeName("AI RAG 맞춤 추천")
                .planName(planName)
                .recommendedCategoryCode(webform.getSelectedPriorityCategory())
                .recommendedCategoryName(ragTextConverter.getKoreanLabel(webform.getSelectedPriorityCategory()))
                .desiredBudgetCode(webform.getDesiredBudgetCode())
                .budgetRangeName(budgetRangeName)
                .totalPremium(currentTotalPremium)
                .recommendReason(recommendReasonStr)
                .coverages(finalizedCoverages)
                .scoreDetails(scoreDetails)
                .scriptContent(scriptContentText)
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    private int parseBudgetCode(String code) {
        return switch (code) {
            case "01" -> 30000;
            case "02" -> 50000;
            case "03" -> 100000;
            default -> 50000;
        };
    }

    private String parseBudgetRangeName(String code) {
        return switch (code) {
            case "01" -> "1~3만원대";
            case "02" -> "3~5만원대";
            case "03" -> "5만원대 이상";
            default -> "상담 후 결정";
        };
    }
}
