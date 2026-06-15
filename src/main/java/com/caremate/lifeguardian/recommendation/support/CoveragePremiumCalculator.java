package com.caremate.lifeguardian.recommendation.support;

import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoveragePremiumCalculator {

	public void applyAgeAdjustedPremium(
			List<CoverageCandidateDto> candidates,
			String ageGroupCode
	) {
		// 연령대 코드에 해당하는 보험료 가중치 조회
		double factor = resolveAgePremiumFactor(ageGroupCode);

		// 후보 담보별 기본 보험료에 연령대별 가중치를 적용
		for (CoverageCandidateDto coverage : candidates) {
			int adjustedPremium =
					(int) Math.round(coverage.getUnitPremium() * factor);

			// 계산된 보험료를 추천 과정에서 사용할 보험료로 반영
			coverage.setUnitPremium(adjustedPremium);
		}
	}

	// 나이가 높아질수록 사고·질병 위험도가 증가한다고 보고 보험료 배수를 점진적으로 높게 설정한다.
	private double resolveAgePremiumFactor(String ageGroupCode) {
		return switch (ageGroupCode) {
			case "AGE_01" -> 1.00;
			case "AGE_02" -> 1.20;
			case "AGE_03" -> 1.50;
			case "AGE_04" -> 2.00;
			default -> 1.00;
		};
	}
}