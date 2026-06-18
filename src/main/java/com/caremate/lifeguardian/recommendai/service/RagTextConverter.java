package com.caremate.lifeguardian.recommendai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagTextConverter {

    private final ObjectMapper objectMapper;

    // 공유해주신 RAG 모델용 쿼리 텍스트 변환 매핑 테이블 데이터 바인딩
    private static final Map<String, String> RAG_DICTIONARY = Map.ofEntries(
            Map.entry("CAT_HOSP_SURG", "입원 및 수술비 보장 관련"),
            Map.entry("CAT_SH_INJURY", "골절, 깁스, 화상 등 상해 치료 보장 관련"),
            Map.entry("CAT_INFECT_GROUP", "어린이 다빈도 질환, 독감, 수족구, 응급실 보장 관련"),
            Map.entry("CAT_DENT_EYE", "치아 보존/보철 치료 및 시력 교정/치료 보장 관련"),
            Map.entry("CAT_CRITICAL_BAL", "암, 뇌혈관, 심혈관 중증 질환 진단 보장 관련"),

            Map.entry("HISTORY_INFECT_DISEASE", "감염성 질환(독감, 수족구, 열성경련 등)"),
            Map.entry("HISTORY_MINOR_ACCIDENT", "가벼운 상해 외상(응급실 내원, 단순 타박상 등)"),
            Map.entry("HISTORY_RESPIRATORY_FEVER", "호흡기 및 전신 고열 질환(폐렴, 천식, 크룹 등)"),
            Map.entry("HISTORY_EYE_DENTAL", "안과 및 치과 질환(시력 저하, 충치, 부정교합 등)"),
            Map.entry("HISTORY_GROWTH_MENTAL", "성장 발달 및 정서 질환(성조숙증, ADHD, 틱장애 등)"),

            Map.entry("ACTIVITY_SCOOTER_BICYCLE", "킥보드, 자전거 등 바퀴 달린 기구 이용 및 도로 위험 노출"),
            Map.entry("ACTIVITY_CONTACT_SPORTS", "축구, 농구, 태권도 등 신체 활동 및 타박상 위험 운동"),
            Map.entry("ACTIVITY_INDOOR_STATIC", "게임, 독서, 악기 등 실내 정적 활동 위주 및 시력 저하 노출"),
            Map.entry("ACTIVITY_GROUP_LIFE", "학교 및 유치원 등 단체 생활 및 감염 질환과 학교 폭력 위험 노출"),

            Map.entry("SURGERY_CONGENITAL_DISEASE", "과거 선천성 이상 및 신생아기 관련 입원/수술 이력 있음"),
            Map.entry("SURGERY_MAJOR_ACCIDENT", "과거 대형 사고, 추락, 중증 화상 등 상해로 인한 입원/수술 이력 있음"),
            Map.entry("SURGERY_DISEASE_HOSPITAL", "과거 질병 수술, 간병인 필요 질환으로 인한 입원/수술 이력 있음"));

    /**
     * DB에서 원천 조회된 정형 코드 명세들을 하나의 흐름을 가진 한글 문장 묶음으로 직렬화합니다.
     */
    public String convertToEmbeddingQuery(String selectedCategory, String historyJson, String activityJson,
            int pastSurgery) {
        StringBuilder queryBuilder = new StringBuilder();

        // 1. STEP 1 최우선 카테고리 플랜 명세 매핑
        if (RAG_DICTIONARY.containsKey(selectedCategory)) {
            queryBuilder.append(RAG_DICTIONARY.get(selectedCategory)).append(" \n");
        }

        // 2. STEP 2 Q1 통원/진단 이력 JSON 어레이 매핑
        List<String> historyCodes = parseJsonArray(historyJson);
        for (String code : historyCodes) {
            if (RAG_DICTIONARY.containsKey(code)) {
                queryBuilder.append(RAG_DICTIONARY.get(code)).append(" \n");
            }
        }

        // 3. STEP 2 Q2 라이프스타일 활동 성향 JSON 어레이 매핑
        List<String> activityCodes = parseJsonArray(activityJson);
        for (String code : activityCodes) {
            if (RAG_DICTIONARY.containsKey(code)) {
                queryBuilder.append(RAG_DICTIONARY.get(code)).append(" \n");
            }
        }

        return queryBuilder.toString().trim();
    }

    /**
     * JSON 문자열을 리스트 객체로 해제 및 파싱합니다.
     */
    public List<String> parseJsonArray(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank() || jsonStr.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.error("[JSON 매핑 해제 실패] 원천 스트림 데이터 결함: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getKoreanLabel(String key) {
        return RAG_DICTIONARY.getOrDefault(key, key);
    }
}