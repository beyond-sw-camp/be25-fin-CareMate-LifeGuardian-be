package com.caremate.lifeguardian.recommendai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageDto {
    private Long coverageId;
    private String coverageName;
    private String categoryCode;
    private String categoryName;
    private Integer unitPremium;
    private Integer selectedOrder;
    private String coverageSummary;
    private List<String> exclusionReasons;
}
