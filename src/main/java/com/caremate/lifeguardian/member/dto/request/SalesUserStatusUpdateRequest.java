package com.caremate.lifeguardian.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUserStatusUpdateRequest {
    // statusCode 수집을 위한 Request DTO
    @NotBlank(message = "변경할 상태 코드는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(01|02)$", message = "유효하지 않은 상태 코드입니다. (01 또는 02만 가능)")
    private String statusCode;
}
