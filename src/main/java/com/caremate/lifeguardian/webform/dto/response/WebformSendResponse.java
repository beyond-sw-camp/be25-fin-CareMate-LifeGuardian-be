package com.caremate.lifeguardian.webform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 웹폼 발송 응답 DTO
 *
 * 웹폼 개별 발송 및 일괄 발송 결과를 내려준다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "웹폼 발송 응답")
public class WebformSendResponse {

    @Schema(description = "웹폼 발행 ID", example = "1")
    private Long issuanceId;

    @Schema(description = "대상 고객 ID", example = "26")
    private Long customerId;

    @Schema(description = "고객 상태 코드 / 01=잠재고객, 02=통합고객", example = "01")
    private String conversionStatusCode;

    @Schema(description = "웹폼 UUID 토큰", example = "9f4a2c1e-7b3a-4e21-91b8-1c9e6f3a4d21")
    private String uuidToken;

    @Schema(description = "웹폼 상태 코드 / 01=미발송, 02=발송완료, 03=작성완료, 04=회수/만료", example = "02")
    private String webformStatusCode;

    @Schema(description = "웹폼 상태명", example = "발송완료")
    private String webformStatusName;

    @Schema(description = "웹폼 발송 일시", example = "2026-06-15T18:30:00")
    private LocalDateTime issuedAt;
}
