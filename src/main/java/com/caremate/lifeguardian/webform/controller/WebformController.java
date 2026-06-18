package com.caremate.lifeguardian.webform.controller;

import com.caremate.lifeguardian.common.ApiResponse;
import com.caremate.lifeguardian.webform.dto.response.WebformSendResponse;
import com.caremate.lifeguardian.webform.service.WebformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "웹폼 API", description = "웹폼 발송 및 회수 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webforms")
public class WebformController {

    private final WebformService webformService;

    /**
     * 웹폼 개별 발송 API
     *
     * @param customerId 웹폼 발송 대상 고객 ID
     * @return 웹폼 발송 결과
     */
    @Operation(summary = "웹폼 개별 발송", description = "선택한 고객에게 웹폼을 발송합니다.")
    @PostMapping("/{customerId}/send")
    public ApiResponse<WebformSendResponse> sendWebform(
            @PathVariable Long customerId
    ) {

        WebformSendResponse response =
                webformService.sendWebform(customerId);

        return ApiResponse.success(
                200,
                "웹폼 발송에 성공했습니다.",
                response
        );
    }

    /**
     * 웹폼 일괄 발송 API
     *
     * 서버가 직접 오늘 웹폼 발송 대상 고객을 조회한다.
     */
    @Operation(summary = "웹폼 일괄 발송", description = "오늘 웹폼 발송 대상 고객에게 웹폼을 일괄 발송합니다.")
    @PostMapping("/send/bulk")
    public ApiResponse<List<WebformSendResponse>> sendBulkWebform() {

        List<WebformSendResponse> response =
                webformService.sendBulkWebform();

        return ApiResponse.success(
                200,
                "웹폼 일괄 발송에 성공했습니다.",
                response
        );
    }

    /**
     * 웹폼 회수 처리 API
     *
     * @param uuidToken 웹폼 UUID 토큰
     * @return 처리 결과
     */
    @Operation(summary = "웹폼 회수 처리", description = "웹폼을 회수 처리하고 회수일을 저장하며, 잠재고객 상담 상태를 상담중으로 변경합니다.")
    @PatchMapping("/{uuidToken}/collect")
    public ApiResponse<Void> collectWebform(
            @PathVariable String uuidToken
    ) {

        webformService.collectWebform(uuidToken);

        return ApiResponse.success(
                200,
                "웹폼 회수 처리에 성공했습니다.",
                null
        );
    }
}
