package com.caremate.lifeguardian.recommendai.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.recommendai.dto.request.RecommendAiRequest;
import com.caremate.lifeguardian.recommendai.dto.response.RecommendAiResponse;
import com.caremate.lifeguardian.recommendai.service.RecommendationRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI RAG 보험 추천 API", description = "웹폼 응답 문맥과 특약 약관을 분석하여 AI 기반 맞춤형 보험 설계를 제안하는 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/recommendai")
@RequiredArgsConstructor
public class RecommendAiController {

    private final RecommendationRagService recommendationRagService;

    @Operation(
            summary = "AI 추천 포트폴리오 생성",
            description = "제출된 웹폼 ID를 받아 한글 문맥으로 변환한 뒤, AI 벡터 검색 및 예산 컷오프 연산을 거쳐 최종 포트폴리오를 구성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendAiResponse>> createRecommendation(
            @RequestBody RecommendAiRequest request
    ) {
        log.info("Received request for AI RAG recommendation - customerId: {}", request.getCustomerId());

        RecommendAiResponse response = recommendationRagService.analyzeAndBuildPortfolio(request.getCustomerId());

        log.info("AI RAG recommendation processed successfully - planId: {}, totalPremium: {}", 
                response.getInsurancePlanId(), response.getTotalPremium());

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "AI RAG 맞춤 추천 포트폴리오 생성에 성공했습니다.",
                        response
                )
        );
    }
}
