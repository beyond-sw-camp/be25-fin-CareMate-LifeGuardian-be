package com.caremate.lifeguardian.recommendai.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class RecommendAiClient {

    private final RestClient restClient;

    public RecommendAiClient(@Value("${aws.lambda.recommend-url:http://localhost:8000/recommend}") String recommendUrl) {
        log.info("AI Recommendation Lambda Client initialized with URL: {}", recommendUrl);
        this.restClient = RestClient.builder()
                .baseUrl(recommendUrl)
                .build();
    }

    public List<String> getRecommendedRiders(String queryText, int age) {
        try {
            LambdaRequest request = new LambdaRequest(queryText, age);

            LambdaResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LambdaResponse.class);

            if (response != null && response.getRecommendedRiders() != null) {
                return response.getRecommendedRiders();
            }
            throw new BaseException(500, "AI 추천 서버로부터 올바른 응답을 받지 못했습니다.");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to connect or fetch recommendation from AI Lambda server: {}", e.getMessage(), e);
            throw new BaseException(500, "AI 추천 서버와의 연결에 실패했습니다. (서버 상태를 확인해 주세요)");
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LambdaRequest {
        private String queryText;
        private int age;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LambdaResponse {
        private List<String> recommendedRiders;
    }
}
