package com.caremate.lifeguardian.script.service;

import com.caremate.lifeguardian.script.dto.ScriptContextDto;
import com.caremate.lifeguardian.script.dto.response.CustomerScriptResponse;
import com.caremate.lifeguardian.script.mapper.CustomerScriptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerScriptServiceImpl implements CustomerScriptService {

	private final CustomerScriptMapper customerScriptMapper;
	private final ScriptPromptBuilder scriptPromptBuilder;
	private final OpenAiScriptService openAiScriptService;

	@Override
	public CustomerScriptResponse getOrCreateTodayScript(
			Long salesUserId,
			Long customerId
	) {
		validateCustomerOwner(salesUserId, customerId);

		// 1. 오늘의 연락 고객 액션 아이템 조회
		Long actionItemId =
				customerScriptMapper.findTodayActionItemIdByCustomerId(customerId);

		// 오늘 액션 아이템이 없으면 스크립트도 생성하지 않음
		if (actionItemId == null) {
			return null;
		}

		// 2. 오늘 액션 아이템 기준으로 기존 스크립트 조회
		CustomerScriptResponse existingScript =
				customerScriptMapper.findScriptByActionItemId(actionItemId);

		if (existingScript != null) {
			return existingScript;
		}

		// 3. 오늘 액션 아이템 기반으로 프롬프트 생성에 필요한 정보 조회
		ScriptContextDto context =
				customerScriptMapper.findScriptContext(actionItemId);

		if (context == null) {
			throw new IllegalArgumentException("액션 아이템 정보를 찾을 수 없습니다.");
		}

		if (!context.getCustomerId().equals(customerId)) {
			throw new IllegalArgumentException("고객과 액션 아이템 정보가 일치하지 않습니다.");
		}

		// 4. 프롬프트 생성
		String prompt = scriptPromptBuilder.build(context);

		// 5. OpenAI 호출
		String scriptContent =
				openAiScriptService.generate(prompt);

		// 6. 오늘 액션 아이템 ID와 함께 스크립트 저장
		customerScriptMapper.insertCustomerScript(
				context.getCustomerId(),
				context.getConversionStatusCode(),
				actionItemId,
				scriptContent
		);

		// 7. 저장된 스크립트 반환
		return customerScriptMapper.findScriptByActionItemId(actionItemId);
	}

	private void validateCustomerOwner(
			Long salesUserId,
			Long customerId
	) {
		boolean exists =
				customerScriptMapper.existsCustomerBySalesUserId(
						salesUserId,
						customerId
				);

		if (!exists) {
			throw new IllegalArgumentException("해당 고객에 대한 접근 권한이 없습니다.");
		}
	}
}
