package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

@Getter
@Setter
@Alias("GrowthStandardDto")
public class GrowthStandardDto {

    private Integer ageMonth;
    private BigDecimal heightP5;
    private BigDecimal heightP50;
    private BigDecimal heightP95;
    private BigDecimal weightP5;
    private BigDecimal weightP50;
    private BigDecimal weightP95;
    private BigDecimal childHeight;
    private BigDecimal childWeight;
    private Integer heightP5Width;
    private Integer heightP50Width;
    private Integer heightP95Width;
    private Integer childHeightWidth;
    private Integer weightP5Width;
    private Integer weightP50Width;
    private Integer weightP95Width;
    private Integer childWeightWidth;
}
