package com.caremate.lifeguardian.recommendation.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.recommendation.domain.InsurancePlan;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.caremate.lifeguardian.recommendation.dto.CategoryScoreDto;
import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.CustomerInfoDto;
import com.caremate.lifeguardian.recommendation.dto.RecommendationResult;
import com.caremate.lifeguardian.recommendation.dto.TemplateReasonResult;
import com.caremate.lifeguardian.recommendation.mapper.RecommendationMapper;
import com.caremate.lifeguardian.recommendation.support.CoverageBudgetPacker;
import com.caremate.lifeguardian.recommendation.support.CoveragePremiumCalculator;
import com.caremate.lifeguardian.recommendation.support.CoverageScoreCalculator;
import com.caremate.lifeguardian.recommendation.support.RecommendationMetaResolver;
import com.caremate.lifeguardian.recommendation.support.WebformAnswerParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationEngine {

	private final RecommendationMapper recommendationMapper;
	private final RecommendationReasonTemplateService reasonTemplateService;
	private final RecommendationMetaResolver metaResolver;
	private final WebformAnswerParser webformAnswerParser;
	private final CoveragePremiumCalculator coveragePremiumCalculator;
	private final CoverageScoreCalculator coverageScoreCalculator;
	private final CoverageBudgetPacker coverageBudgetPacker;

	public RecommendationResult run(Long customerId) {

		// 고객의 최신 웹폼 응답을 조회한다.
		WebformResponse webform = recommendationMapper.findLatestWebform(customerId);

		if (webform == null) {
			throw new BaseException(404, "웹폼이 제출되지 않은 고객입니다.");
		}

		// 추천 대상 고객의 기본 정보를 조회한다.
		CustomerInfoDto customer = recommendationMapper.findCustomerInfo(customerId);

		if (customer == null) {
			throw new BaseException(404, "고객 정보가 없습니다.");
		}

		// 고객 생년월일을 기준으로 현재 만나이를 계산한다.
		int age = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();

		// 나이를 질병 통계 기준 연령대 코드로 변환한다.
		String ageGroupCode = metaResolver.resolveAgeGroupCode(age);

		// 웹폼에서 고객이 선택한 최우선 관심 카테고리를 메인 카테고리로 사용한다.
		String mainCategoryCode = webform.getSelectedPriorityCategory();

		// 웹폼의 병력/활동 JSON 데이터를 파싱하여 답변 코드 목록으로 변환한다.
		List<String> answerCodes = webformAnswerParser.parse(webform);

		// 문진 답변을 기반으로 카테고리별 위험 점수를 계산한다.
		// question_mapping 테이블을 조회하여 답변 코드가 어떤 보장 카테고리와 연결되는지 확인한다.
		List<CategoryScoreDto> categoryScores =
				answerCodes.isEmpty()
						? List.of()
						: recommendationMapper.findCategoryScores(answerCodes);

		// 메인 카테고리를 제외한 문진 기반 서브 카테고리 1개를 선정한다.
		List<String> subCategoryCodes =
				categoryScores.stream()
						.filter(score -> !score.getCategoryCode().equals(mainCategoryCode))
						.sorted(Comparator.comparing(CategoryScoreDto::getScore).reversed())
						.limit(1)
						.map(CategoryScoreDto::getCategoryCode)
						.toList();

		// 최종 추천 대상 카테고리 목록을 구성한다.
		List<String> targetCategoryCodes = new ArrayList<>();
		targetCategoryCodes.add(mainCategoryCode);
		targetCategoryCodes.addAll(subCategoryCodes);

		// 고객 나이에 가입 가능한 후보 담보를 조회한다.
		// insurance_coverage의 min_target_age, max_target_age 조건을 기준으로 필터링한다.
		List<CoverageCandidateDto> candidates =
				recommendationMapper.findCandidateCoverages(targetCategoryCodes, age);

		// 연령대별 위험 가중치를 반영하여 담보 보험료를 조정한다.
		// insurance_coverage.unit_premium을 기본 보험료로 보고,
		// ageGroupCode별 배수를 적용하여 최종 적용 보험료를 계산한다.
		coveragePremiumCalculator.applyAgeAdjustedPremium(
				candidates,
				ageGroupCode
		);

		// 후보 담보별 추천 점수를 계산한다.
		// BaseScore는 질병 통계 순위를 기반으로 계산
		// BiasScore는 고객이 선택한 메인 카테고리와 문진 기반 서브 카테고리에 따라 부여한다.
		coverageScoreCalculator.applyScores(
				candidates,
				ageGroupCode,
				customer.getGender(),
				mainCategoryCode,
				subCategoryCodes
		);

		// 최종 추천 점수가 높은 순서로 담보를 정렬한다.
		// 점수가 같으면 dangerPriorityOrder가 낮은 담보를 우선한다.
		List<CoverageCandidateDto> sortedCoverages =
				candidates.stream()
						.sorted(
								Comparator
										.comparing(CoverageCandidateDto::getFinalScore)
										.reversed()
										.thenComparing(CoverageCandidateDto::getDangerPriorityOrder)
						)
						.toList();

		// 웹폼에서 선택한 예산 코드를 실제 최대 예산 금액으로 변환한다.
		// 점수가 같으면 dangerPriorityOrder가 낮은 담보를 우선한다.
		int maxBudget =
				metaResolver.resolveBudgetAmount(webform.getDesiredBudgetCode());

		// 예산 범위 안에서 최종 담보 조합을 구성한다.
		// 메인 카테고리와 서브 카테고리 담보를 균형 있게 선택하면서 maxBudget을 넘지 않도록 한다.
		List<CoverageCandidateDto> selectedCoverages =
				coverageBudgetPacker.packBalanced(
						sortedCoverages,
						mainCategoryCode,
						subCategoryCodes,
						maxBudget
				);

		// 선택된 담보들의 최종 보험료 합계를 계산한다.
		// 이 값은 insurance_plan.total_premium으로 저장된다.
		int totalPremium =
				selectedCoverages.stream()
						.mapToInt(CoverageCandidateDto::getUnitPremium)
						.sum();

		// 선택된 담보들의 최종 추천 점수 합계를 계산한다.
		int totalScore =
				selectedCoverages.stream()
						.mapToInt(CoverageCandidateDto::getFinalScore)
						.sum();

		// 추천 이유와 상담 스크립트 데이터를 생성한다.
		// recommendReason은 API 응답으로 내려가고,
		// scriptData는 DB에 JSON 형태로 저장된다.
		TemplateReasonResult templateResult =
				reasonTemplateService.createReason(
						customer,
						mainCategoryCode,
						subCategoryCodes,
						selectedCoverages,
						totalPremium,
						totalScore
				);

		// 최종 추천 플랜 객체를 생성한다.
		// 아직 DB에 저장되기 전 상태이며,
		// Service 계층에서 insertInsurancePlan 호출 시 insurance_plan 테이블에 저장된다.
		InsurancePlan insurancePlan =
				InsurancePlan.builder()
						.webformResponseId(webform.getId())
						.planName(metaResolver.resolvePlanName(mainCategoryCode))
						.totalPremium(totalPremium)
						.scriptData(templateResult.getScriptData())
						.recommendationTypeCode("01")
						.build();

		// 추천 엔진 실행 결과를 하나의 내부 결과 DTO로 반환한다.
		// Service 계층은 이 결과를 기반으로 플랜, 담보, 추천 이력을 DB에 저장하고 응답 DTO를 생성한다.
		return RecommendationResult.builder()
				.customerId(customerId)
				.customer(customer)
				.webformResponse(webform)
				.mainCategoryCode(mainCategoryCode)
				.subCategoryCodes(subCategoryCodes)
				.selectedCoverages(selectedCoverages)
				.insurancePlan(insurancePlan)
				.totalScore(totalScore)
				.recommendReason(templateResult.getRecommendReason())
				.scriptData(templateResult.getScriptData())
				.build();
	}
}