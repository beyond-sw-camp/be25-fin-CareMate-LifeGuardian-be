package com.caremate.lifeguardian.recommendation.mapper;

import com.caremate.lifeguardian.recommendation.domain.InsurancePlan;
import com.caremate.lifeguardian.recommendation.domain.RecommendationLog;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.recommendation.dto.CategoryScoreDto;
import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.CustomerInfoDto;
import com.caremate.lifeguardian.recommendation.dto.DiseaseRankDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {

	int countCustomerBySalesUser(
			@Param("customerId") Long customerId,
			@Param("salesUserId") Long salesUserId
	);

	boolean existsPotentialCustomer(Long customerId);

	boolean existsIntegratedCustomer(Long customerId);

	int countPotentialCustomerBySalesUser(Long customerId, Long salesUserId);

	int countIntegratedCustomerBySalesUser(Long customerId, Long salesUserId);

	WebformResponse findLatestWebform(
			@Param("customerId") Long customerId
	);

	CustomerInfoDto findCustomerInfo(
			@Param("customerId") Long customerId
	);

	List<CategoryScoreDto> findCategoryScores(
			@Param("answerCodes") List<String> answerCodes
	);

	List<CoverageCandidateDto> findCandidateCoverages(
			@Param("categoryCodes") List<String> categoryCodes,
			@Param("age") int age
	);

	List<DiseaseRankDto> findDiseaseRanks(
			@Param("coverageId") Long coverageId,
			@Param("ageGroupCode") String ageGroupCode,
			@Param("gender") String gender
	);

	String findCategoryNameByCode(
			@Param("categoryCode") String categoryCode
	);

	void insertInsurancePlan(
			InsurancePlan insurancePlan
	);

	void insertPlanCoverages(
			@Param("planId") Long planId,
			@Param("coverages") List<CoverageCandidateDto> coverages
	);

	void insertRecommendationLog(
			RecommendationLog recommendationLog
	);
}