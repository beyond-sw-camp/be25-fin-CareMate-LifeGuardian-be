package com.caremate.lifeguardian.script.service;

import com.caremate.lifeguardian.scheduler.dailyaction.constant.TriggerTypeCode;
import com.caremate.lifeguardian.script.dto.ScriptContextDto;
import org.springframework.stereotype.Component;

@Component
public class ScriptPromptBuilder {

	public String build(ScriptContextDto context) {
		return switch (context.getTriggerTypeCode()) {
			case TriggerTypeCode.CHILD_SHIFT_D30 ->
					base(context, "자녀 상령일이 30일 남았습니다.", "보험 나이 변경 전 보장 점검을 자연스럽게 제안");
			case TriggerTypeCode.CHILD_SHIFT_D7 ->
					base(context, "자녀 상령일이 7일 남았습니다.", "상령일 임박에 따른 빠른 상담 유도");
			case TriggerTypeCode.FAMILY_REMODELING ->
					base(context, "자녀와 부모 조건이 동시에 충족되었습니다.", "가족 단위 보장 리모델링 제안");
			case TriggerTypeCode.CHILD_COVERAGE_CHECK ->
					base(context, "자녀 보장 점검이 필요한 상황입니다.", "자녀 생애주기 변화에 따른 보장 점검");
			case TriggerTypeCode.PARENT_HEALTH_CHECK ->
					base(context, "부모 건강 보장 점검이 필요한 상황입니다.", "부모 연령 변화에 따른 건강 보장 점검");
			case TriggerTypeCode.PARENT_SHIFT_D30 ->
					base(context, "부모 상령일이 30일 남았습니다.", "보험 나이 변경 전 부모 보장 점검");
			case TriggerTypeCode.PARENT_SHIFT_D7 ->
					base(context, "부모 상령일이 7일 남았습니다.", "상령일 임박 상담 유도");
			case TriggerTypeCode.CHILD_TURN_7 ->
					base(context, "자녀가 7세 연령대로 전환됩니다.", "초등학교 입학 전후 생활 사고 보장 점검");
			case TriggerTypeCode.CHILD_TURN_14 ->
					base(context, "자녀가 14세 연령대로 전환됩니다.", "청소년기 활동 증가와 어린이보험 졸업 전 점검");
			case TriggerTypeCode.PARENT_TURN_30 ->
					base(context, "부모가 30세 연령대로 전환됩니다.", "성인 건강 보장 점검");
			case TriggerTypeCode.PARENT_TURN_50 ->
					base(context, "부모가 50세 연령대로 전환됩니다.", "암, 뇌혈관, 심혈관 보장 점검");
			case TriggerTypeCode.PARENT_TURN_60 ->
					base(context, "부모가 60세 연령대로 전환됩니다.", "노후, 간병, 장기요양 보장 점검");
			default ->
					base(context, "보험 보장 점검이 필요한 상황입니다.", "현재 보장 상태 점검");
		};
	}

	private String base(ScriptContextDto c, String situation, String point) {
		return """
				너는 보험 영업사원의 상담 스크립트 작성 도우미야.
				
				    [고객 정보]
				    자녀명: %s
				    부모명: %s
				    액션 사유: %s
				
				    [반드시 반영해야 할 상담 상황]
				    %s
				
				    [반드시 강조해야 할 포인트]
				    %s
				
				    [작성 목적]
				    부모 고객이 상담의 필요성을 이해하고 자연스럽게 상담 일정을 잡을 수 있도록 돕는다.
				
				    [작성 조건]
				    - 반드시 액션 사유와 상담 상황을 첫 두 문장 안에 자연스럽게 포함
				    - 부모에게 연락하는 보험 설계사 말투로 작성
				    - 고객의 현재 상황을 이해하고 있다는 느낌으로 작성
				    - 왜 연락드렸는지 명확하게 설명
				    - 고객이 얻을 수 있는 이점을 1~2문장 포함
				    - 보험나이, 생애주기 변화, 보장 점검의 용어 설명
				    - 보험나이, 생애주기 변화, 보장 점검 필요성 등을 자연스럽게 설명
				    - 과장 표현 금지
				    - 불안감을 과도하게 조성하지 말 것
				    - 단순 상품 판매가 아닌 보장 점검 상담처럼 작성
				    - 6~8문장 정도의 자연스러운 문자 상담 스크립트
				    - 마지막 문장은 상담 가능 여부를 묻는 형태
				
				    [좋은 예시]
				    안녕하세요, 이성민님.
				    송지호님의 보험나이 변경 기준일(상령일)이 7일 앞으로 다가왔습니다.
				    상령일 이후에는 동일한 보장이라도 보험료가 달라질 수 있어 미리 보장 상태를 점검해보시는 것이 좋습니다.
				    특히 성장기 자녀의 경우 연령 변화에 따라 필요한 보장 범위가 달라질 수 있기 때문에 현재 가입된 보장과 부족한 보장 항목을 함께 확인해보실 것을 권장드립니다.
				    상담을 통해 송지호님의 연령과 현재 상황에 맞는 보장 준비 방향을 안내드리겠습니다.
				    편하신 시간에 간단히 상담 가능하실까요?
				
				    [출력 형식]
				    상담 스크립트 본문만 작성
				""".formatted(
				c.getChildName(),
				c.getParentName(),
				c.getTriggerName(),
				situation,
				point
		);
	}
}