package com.caremate.lifeguardian.recommendation.support;


import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.DiseaseRankDto;
import com.caremate.lifeguardian.recommendation.mapper.RecommendationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoverageScoreCalculator {

	private static final int MAX_RANK_BASE = 21;
	private static final int RANK_SCORE_UNIT = 5;
	private static final int MAIN_CATEGORY_BIAS_SCORE = 50;
	private static final int SUB_CATEGORY_BIAS_SCORE = 20;

	private final RecommendationMapper recommendationMapper;

	/**
	 * 후보 담보 목록에 추천 점수를 부여한다.
	 *
	 * 각 담보별로 질병 통계 기반 BaseScore와
	 * 고객 관심사 기반 BiasScore를 계산한 뒤,
	 * 두 점수를 합산하여 FinalScore를 설정한다.
	 */
	public void applyScores(
			List<CoverageCandidateDto> candidates,
			String ageGroupCode,
			String gender,
			String mainCategoryCode,
			List<String> subCategoryCodes
	) {
		for (CoverageCandidateDto coverage : candidates) {

			// 담보와 연결된 질병 통계 순위 데이터를 조회한다.
			// 연령대와 성별을 기준으로 여러 연도의 rank 데이터를 가져온다.
			List<DiseaseRankDto> ranks =
					recommendationMapper.findDiseaseRanks(
							coverage.getCoverageId(),
							ageGroupCode,
							gender
					);

			// 질병 통계 순위를 기반으로 BaseScore 계산
			int baseScore = calculateBaseScore(ranks);

			// 고객 선택 카테고리와 문진 기반 카테고리를 반영하여 BiasScore 계산
			int biasScore =
					calculateBiasScore(
							coverage.getCategoryCode(),
							mainCategoryCode,
							subCategoryCodes
					);

			// 최종 추천 점수 반영
			coverage.setBaseScore(baseScore);
			coverage.setBiasScore(biasScore);
			coverage.setFinalScore(baseScore + biasScore);
		}
	}

	/**
	 * 다년도 질병 통계 순위를 기반으로 BaseScore를 계산한다.
	 *
	 * 순위가 높을수록 위험도가 높다고 보고,
	 * rank 1위는 100점, rank 20위는 5점이 되도록 계산한다.
	 *
	 * 예)
	 * rank 1위  → (21 - 1) * 5 = 100점
	 * rank 10위 → (21 - 10) * 5 = 55점
	 * rank 20위 → (21 - 20) * 5 = 5점
	 *
	 * 여러 연도 데이터가 존재하면 연도별 가중치를 적용한 평균 점수를 사용한다.
	 */
	private int calculateBaseScore(List<DiseaseRankDto> ranks) {
		if (ranks == null || ranks.isEmpty()) {
			return 0;
		}

		double totalWeightedScore = 0;
		double totalWeight = 0;

		for (DiseaseRankDto rank : ranks) {
			// 해당 연도의 질병 순위를 점수로 변환
			int yearScore =
					(MAX_RANK_BASE - rank.getRankByAgeGender()) * RANK_SCORE_UNIT;

			// 최근 연도일수록 높은 가중치 적용
			double weight =
					resolveYearWeight(rank.getDataYear());

			// 연도별 점수에 가중치를 곱해 누적
			totalWeightedScore += yearScore * weight;
			totalWeight += weight;
		}

		if (totalWeight == 0) {
			return 0;
		}

		// 가중 평균 점수를 정수로 반올림하여 반환
		return (int) Math.round(totalWeightedScore / totalWeight);
	}

	/**
	 * 고객 선택 정보 기반 BiasScore를 계산한다.
	 *
	 * 메인 카테고리는 고객이 웹폼에서 직접 선택한 최우선 관심 영역이므로
	 * 높은 가중치를 부여한다.
	 *
	 * 서브 카테고리는 문진 답변을 통해 추가로 도출된 위험 영역이므로
	 * 보조 가중치를 부여한다.
	 */
	private int calculateBiasScore(
			String categoryCode,
			String mainCategoryCode,
			List<String> subCategoryCodes
	) {
		int biasScore = 0;

		// 고객이 직접 선택한 메인 카테고리 담보인 경우 높은 가중치 부여
		if (categoryCode.equals(mainCategoryCode)) {
			biasScore += MAIN_CATEGORY_BIAS_SCORE;
		}

		// 문진 답변으로 도출된 서브 카테고리 담보인 경우 보조 가중치 부여
		if (subCategoryCodes.contains(categoryCode)) {
			biasScore += SUB_CATEGORY_BIAS_SCORE;
		}

		return biasScore;
	}

	/**
	 * 통계 데이터 연도별 가중치를 반환한다.
	 *
	 * 최근 질병 통계가 현재 위험도를 더 잘 반영한다고 보고
	 * 최신 연도일수록 높은 가중치를 적용한다.
	 */
	private double resolveYearWeight(String dataYear) {
		return switch (dataYear) {
			case "2025" -> 0.5; // 최신 데이터: 가장 높은 반영 비율
			case "2024" -> 0.3;
			case "2023" -> 0.2;
			default -> 0.1;
		};
	}
}