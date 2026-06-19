package com.caremate.lifeguardian.recommendai.mapper;

import com.caremate.lifeguardian.recommendation.domain.InsurancePlan;
import com.caremate.lifeguardian.recommendation.domain.RecommendationLog;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.CustomerInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecommendAiMapper {

    WebformResponse findWebformById(@Param("id") Long id);

    WebformResponse findLatestWebformByCustomerId(@Param("customerId") Long customerId);

    CustomerInfoDto findCustomerInfo(@Param("customerId") Long customerId);

    CoverageCandidateDto findCoverageByName(@Param("coverageName") String coverageName);

    void insertInsurancePlan(InsurancePlan insurancePlan);

    void insertPlanCoverage(
            @Param("planId") Long planId,
            @Param("coverageId") Long coverageId,
            @Param("selectedOrder") int selectedOrder,
            @Param("appliedPremium") int appliedPremium
    );

    void insertRecommendationLog(RecommendationLog recommendationLog);
}
