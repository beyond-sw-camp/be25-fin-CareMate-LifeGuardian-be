package com.caremate.lifeguardian.report.dto.internal.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("DiseaseRiskItemDto")
public class DiseaseRiskItemDto {

    private String diseaseCode;
    private String diseaseName;
    private String treatmentType;
    private Integer patientCount;
    private Integer rank;
    private String categoryCode;
    private String categoryName;
    private String coverageName;
    private String description;
}
