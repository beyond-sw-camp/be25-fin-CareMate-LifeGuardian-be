package com.caremate.lifeguardian.potential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PotentialCustomerUpdateRequest {

    @NotBlank(message = "잠재고객 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "성별은 필수입니다.")
    private String gender;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;
}
