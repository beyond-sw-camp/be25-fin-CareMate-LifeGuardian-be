package com.caremate.lifeguardian.recommendation.support;

import com.caremate.lifeguardian.recommendation.mapper.RecommendationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationMetaResolver {

	private final RecommendationMapper recommendationMapper;

	public int resolveBudgetAmount(String budgetCode) {
		return switch (budgetCode) {
			case "01" -> 30000;
			case "02" -> 60000;
			case "03" -> 90000;
			default -> 50000;
		};
	}

	public String resolveAgeGroupCode(int age) {
		if (age <= 4) {
			return "AGE_01";
		}

		if (age <= 7) {
			return "AGE_02";
		}

		if (age <= 13) {
			return "AGE_03";
		}

		return "AGE_04";
	}

	public String resolveCategoryName(String categoryCode) {
		String categoryName =
				recommendationMapper.findCategoryNameByCode(categoryCode);

		if (categoryName == null || categoryName.isBlank()) {
			return "맞춤 보장";
		}

		return categoryName;
	}

	public String resolvePlanName(String categoryCode) {
		return resolveCategoryName(categoryCode) + " 맞춤 추천 플랜";
	}
}