package com.caremate.lifeguardian.recommendai.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
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
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to connect or fetch recommendation from AI Lambda server: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LambdaRequest {
        private String queryText;
        private int age;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LambdaResponse {
        private List<String> recommendedRiders;
    }
}
