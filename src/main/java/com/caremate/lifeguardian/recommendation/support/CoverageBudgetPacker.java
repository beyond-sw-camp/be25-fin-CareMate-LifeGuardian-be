package com.caremate.lifeguardian.recommendation.support;

import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CoverageBudgetPacker {

	// 한 카테고리 담보만 몰아서 선택하지 않고 메인 카테고리와 서브 카테고리를 번갈아 선택한다.
	public List<CoverageCandidateDto> packBalanced(
			List<CoverageCandidateDto> coverages,
			String mainCategoryCode,
			List<String> subCategoryCodes,
			int maxBudget
	) {
		List<CoverageCandidateDto> selected = new ArrayList<>();

		// 담보를 선택할 카테고리 순서 구성
		// 메인 카테고리를 가장 먼저 보고, 이후 서브 카테고리를 순서대로 확인한다.
		List<String> pickCategoryOrder = new ArrayList<>();
		pickCategoryOrder.add(mainCategoryCode);
		pickCategoryOrder.addAll(subCategoryCodes);

		// 카테고리별 담보 목록으로 그룹화한다.
		// 입력 coverages는 이미 finalScore, dangerPriorityOrder 기준으로 정렬된 상태이므로
		// 각 카테고리 내부에서도 우선순위가 높은 담보가 앞쪽에 위치한다.
		Map<String, List<CoverageCandidateDto>> groupedCoverages =
				coverages.stream()
						.collect(Collectors.groupingBy(
								CoverageCandidateDto::getCategoryCode,
								LinkedHashMap::new,
								Collectors.toList()
						));

		int totalPremium = 0;
		int selectedOrder = 1;
		int round = 0;

		while (true) {
			boolean hasCandidateInThisRound = false;
			boolean selectedInThisRound = false;

			// 현재 round 기준으로 각 카테고리의 n번째 담보를 순서대로 확인한다.
			for (String categoryCode : pickCategoryOrder) {
				List<CoverageCandidateDto> categoryCoverages =
						groupedCoverages.getOrDefault(categoryCode, List.of());

				// 해당 카테고리에 현재 round에 해당하는 담보가 없으면 건너뛴다.
				if (round >= categoryCoverages.size()) {
					continue;
				}

				hasCandidateInThisRound = true;

				CoverageCandidateDto coverage = categoryCoverages.get(round);

				// 현재 담보를 추가해도 최대 예산을 초과하지 않으면 선택한다.
				if (totalPremium + coverage.getUnitPremium() <= maxBudget) {
					coverage.setSelectedOrder(selectedOrder++);
					selected.add(coverage);
					totalPremium += coverage.getUnitPremium();
					selectedInThisRound = true;
				}
			}

			// 모든 카테고리에서 더 이상 확인할 담보가 없으면 종료한다.
			if (!hasCandidateInThisRound) {
				break;
			}

			// 이번 round에서 아무 담보도 선택하지 못했고,
			// 남은 담보들이 모두 잔여 예산을 초과한다면 더 이상 선택할 수 없으므로 종료한다.
			if (!selectedInThisRound &&
					isAllRemainingOverBudget(
							groupedCoverages,
							pickCategoryOrder,
							round + 1,
							maxBudget - totalPremium
					)) {
				break;
			}

			// 다음 순위 담보를 확인하기 위해 round 증가
			round++;
		}

		return selected;
	}

	/**
	 * 남아 있는 모든 담보가 잔여 예산을 초과하는지 확인한다.
	 *
	 * true  → 더 이상 선택 가능한 담보가 없음
	 * false → 아직 선택 가능한 담보가 남아 있음
	 */
	private boolean isAllRemainingOverBudget(
			Map<String, List<CoverageCandidateDto>> groupedCoverages,
			List<String> pickCategoryOrder,
			int startRound,
			int remainBudget
	) {
		for (String categoryCode : pickCategoryOrder) {
			List<CoverageCandidateDto> categoryCoverages =
					groupedCoverages.getOrDefault(categoryCode, List.of());

			for (int i = startRound; i < categoryCoverages.size(); i++) {
				if (categoryCoverages.get(i).getUnitPremium() <= remainBudget) {
					return false;
				}
			}
		}

		return true;
	}
}