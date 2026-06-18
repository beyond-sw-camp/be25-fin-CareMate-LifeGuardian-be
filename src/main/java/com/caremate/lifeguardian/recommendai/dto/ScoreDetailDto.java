package com.caremate.lifeguardian.recommendai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDetailDto {
    private String questionKey;
    private String selectedOptionValue;
    private String categoryCode;
    private String categoryName;
    private String reasonMessage;
}
