package com.caremate.lifeguardian.recommendation.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.recommendation.dto.CoverageCandidateDto;
import com.caremate.lifeguardian.recommendation.dto.CustomerInfoDto;
import com.caremate.lifeguardian.recommendation.dto.TemplateReasonResult;
import com.caremate.lifeguardian.recommendation.support.RecommendationMetaResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationReasonTemplateService {

	private final ObjectMapper objectMapper;
	private final RecommendationMetaResolver metaResolver;

	public TemplateReasonResult createReason(
			CustomerInfoDto customer,
			String mainCategoryCode,
			List<String> subCategoryCodes,
			List<CoverageCandidateDto> selectedCoverages,
			int totalPremium,
			int totalScore
	) {
		// 메인 카테고리 코드를 화면 표시용 카테고리명으로 변환
		String mainCategoryName =
				metaResolver.resolveCategoryName(mainCategoryCode);

		// 문진 결과로 도출된 서브 카테고리 설명 문구 생성
		String subCategoryText =
				subCategoryCodes.isEmpty()
						? "추가 위험 항목 없음"
						: subCategoryCodes.stream()
						.map(metaResolver::resolveCategoryName)
						.collect(Collectors.joining(", "));

		// 최우선 추천 담보 선정
		CoverageCandidateDto topCoverage =
				selectedCoverages.isEmpty()
						? null
						: selectedCoverages.get(0);

		// 추천 담보가 없는 예외 상황을 대비한 기본 문구 설정
		String topCoverageName =
				topCoverage == null
						? "핵심 담보"
						: topCoverage.getCoverageName();

		// 프론트 추천 이유 영역에 표시할 문장 생성
		String recommendReason = String.format(
				"%s 고객은 웹폼에서 '%s' 항목을 최우선 점검 항목으로 선택했습니다. " +
						"문진 답변을 분석한 결과 '%s' 영역도 함께 고려할 필요가 있는 것으로 판단되었습니다. " +
						"질병 통계 기반 BaseScore와 부모 선택 항목 기반 BiasScore를 합산한 결과, " +
						"'%s' 담보가 가장 높은 우선순위로 선정되었습니다. " +
						"최종 추천 플랜은 월 %,d원으로 구성되었으며 총 추천 점수는 %d점입니다.",
				customer.getName(),
				mainCategoryName,
				subCategoryText,
				topCoverageName,
				totalPremium,
				totalScore
		);

		// DB 저장용 상담 스크립트 요약 문구 생성
		String summary = String.format(
				"%s 중심으로 월 %,d원 예산 내 핵심 담보를 추천했습니다.",
				mainCategoryName,
				totalPremium
		);

		// 영업사원이 고객 상담 시 참고할 상담 멘트 생성
		String salesScript = String.format(
				"고객님께서 가장 걱정하신 부분은 '%s' 영역이었습니다. " +
						"그래서 해당 영역을 중심으로 보장을 구성하되, 문진 결과에서 함께 확인된 '%s' 항목도 반영했습니다. " +
						"특히 '%s' 담보는 통계 기반 위험도와 고객님의 우려가 함께 반영되어 우선 추천되었습니다. " +
						"전체 보험료는 월 %,d원 수준으로 예산 안에서 핵심 담보 위주로 구성했습니다.",
				mainCategoryName,
				subCategoryText,
				topCoverageName,
				totalPremium
		);

		try {
			// 추천 이력 복원 및 상담 스크립트 보관을 위한 JSON 데이터 생성
			Map<String, Object> scriptData = new LinkedHashMap<>();
			scriptData.put("summary", summary);
			scriptData.put("salesScript", salesScript);

			// 추천 이유와 DB 저장용 scriptData를 함께 반환
			return TemplateReasonResult.builder()
					.recommendReason(recommendReason)
					.scriptData(objectMapper.writeValueAsString(scriptData))
					.build();

		} catch (Exception e) {
			throw new BaseException(500, "추천 이유 템플릿 생성에 실패했습니다.");
		}
	}
}