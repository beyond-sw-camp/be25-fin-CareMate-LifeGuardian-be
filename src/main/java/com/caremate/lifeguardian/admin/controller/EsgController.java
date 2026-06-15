package com.caremate.lifeguardian.admin.controller;

import com.caremate.lifeguardian.admin.dto.response.EnvironmentalScoresResponse;
import com.caremate.lifeguardian.admin.dto.response.PeakCutProfileResponse;
import com.caremate.lifeguardian.admin.service.EsgService;
import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "ESG 및 인프라 API", description = "ESG 지표 및 인프라 부하를 조회하는 API입니다.")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard/esg")
@RequiredArgsConstructor
public class EsgController {

    private final EsgService esgService;

    @Operation(summary = "환경(E) 지표 누적 스코어보드 조회", description = "누적 탄소 절감량(kg) 및 인프라 비용 절감액(원) 데이터를 조회합니다.")
    @GetMapping("/environmental-scores")
    public ResponseEntity<ApiResponse<EnvironmentalScoresResponse>> getEnvironmentalScores() {
        log.info("환경(E) 지표 누적 스코어보드 조회 API 요청 수신");
        EnvironmentalScoresResponse response = esgService.getEnvironmentalScores();
        log.info("환경(E) 지표 누적 스코어보드 조회 API 처리 성공");

        return ResponseEntity.ok(ApiResponse.success(200, "환경(E) 누적 스코어보드 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "24시간 인프라 부하 및 피크 컷 차트 조회", description = "어제 또는 특정 일자의 0시부터 23시까지의 시간대별 기존 예상 부하율 및 최적화된 실제 부하율을 조회합니다.")
    @GetMapping("/peak-cut-profile")
    public ResponseEntity<ApiResponse<?>> getPeakCutProfile(
            @RequestParam(value = "targetDate", required = false) String targetDate) {
        log.info("24시간 인프라 부하 및 피크 컷 차트 조회 API 요청 수신 - targetDate: {}", targetDate);

        // 날짜 형식 수동 유효성 검증
        List<ErrorResponse> errors = new ArrayList<>();
        if (targetDate != null && !targetDate.isEmpty() && !targetDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            errors.add(ErrorResponse.builder()
                    .field("targetDate")
                    .reason("날짜 형식은 YYYY-MM-DD 포맷이어야 합니다.")
                    .build());
        }

        if (!errors.isEmpty()) {
            log.warn("24시간 인프라 부하 및 피크 컷 차트 조회 API 요청 실패 - 잘못된 날짜 형식: {}", targetDate);
            return ResponseEntity
                    .status(400)
                    .body(ApiResponse.fail(400, "요청 파라미터가 올바르지 않습니다.", errors));
        }

        PeakCutProfileResponse response = esgService.getPeakCutProfile(targetDate);
        log.info("24시간 인프라 부하 및 피크 컷 차트 조회 API 처리 성공");

        return ResponseEntity.ok(ApiResponse.success(200, "ESG 피크 컷 프로파일 조회가 완료되었습니다.", response));
    }
}
