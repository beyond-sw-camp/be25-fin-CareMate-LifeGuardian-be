package com.caremate.lifeguardian.recommendation.support;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.recommendation.domain.WebformResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebformAnswerParser {

	private final ObjectMapper objectMapper;

	public List<String> parse(WebformResponse webform) {
		List<String> answerCodes = new ArrayList<>();

		try {
			if (webform.getHistoryJson() != null) {
				answerCodes.addAll(
						objectMapper.readValue(
								webform.getHistoryJson(),
								new TypeReference<List<String>>() {}
						)
				);
			}

			if (webform.getActivityJson() != null) {
				answerCodes.addAll(
						objectMapper.readValue(
								webform.getActivityJson(),
								new TypeReference<List<String>>() {}
						)
				);
			}

			return answerCodes;

		} catch (Exception e) {
			throw new BaseException(400, "웹폼 JSON 파싱에 실패했습니다.");
		}
	}
}