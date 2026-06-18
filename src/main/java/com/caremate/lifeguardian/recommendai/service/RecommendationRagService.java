package com.caremate.lifeguardian.recommendai.service;

import com.caremate.lifeguardian.recommendai.dto.response.RecommendAiResponse;

public interface RecommendationRagService {

    /**
     * 고객 ID를 기반으로 웹폼 데이터를 분석하고 AI RAG 맞춤 보험 포트폴리오를 조립하여 반환합니다.
     *
     * @param customerId 추천 분석 대상 고객 ID
     * @return AI RAG 추천 결과 DTO
     */
    RecommendAiResponse analyzeAndBuildPortfolio(Long customerId);
}
