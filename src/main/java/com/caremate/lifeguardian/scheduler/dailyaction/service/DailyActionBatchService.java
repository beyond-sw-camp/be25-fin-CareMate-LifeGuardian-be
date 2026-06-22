package com.caremate.lifeguardian.scheduler.dailyaction.service;

import com.caremate.lifeguardian.scheduler.dailyaction.constant.TriggerTypeCode;
import com.caremate.lifeguardian.scheduler.dailyaction.dto.ActionTargetDto;
import com.caremate.lifeguardian.scheduler.dailyaction.mapper.DailyActionBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DailyActionBatchService {

	private final DailyActionBatchMapper dailyActionBatchMapper;

	// 오늘 생성해야 할 영업 액션 아이템 생성
	public void createTodayActionItems(LocalDate today) {

		List<ActionTargetDto> targets =
				dailyActionBatchMapper.findActionTargets();

		for (ActionTargetDto target : targets) {

			log.info("배치 대상 customerId={}, childBirthDate={}, parentBirthDate={}, today={}",
					target.getCustomerId(),
					target.getChildBirthDate(),
					target.getParentBirthDate(),
					today
			);

			boolean childCondition = createChildActions(target, today);
			boolean parentCondition = createParentActions(target, today);

			log.info("조건 결과 customerId={}, childCondition={}, parentCondition={}",
					target.getCustomerId(),
					childCondition,
					parentCondition
			);

			createThreeStepActions(target, today, childCondition, parentCondition);
		}
	}

	// 자녀 조건(상령일, 생애주기) 확인 및 액션 생성
	private boolean createChildActions(ActionTargetDto target, LocalDate today) {

		boolean childCondition = false;

		LocalDate childShiftDate =
				calculateThisYearShiftDate(target.getChildBirthDate(), today);

		LocalDate childBirthday =
				target.getChildBirthDate().withYear(today.getYear());

		if (today.equals(childBirthday)) {
			insertAction(target, TriggerTypeCode.CHILD_BIRTHDAY, 50, today);
		}

		if (today.equals(childShiftDate)) {
			insertAction(target, TriggerTypeCode.CHILD_SHIFT_DDAY, 110, today);
			childCondition = true;
		}

		if (today.equals(childShiftDate.minusDays(30))) {
			insertAction(target, TriggerTypeCode.CHILD_SHIFT_D30, 85, today);
			childCondition = true;
		}

		if (today.equals(childShiftDate.minusDays(7))) {
			insertAction(target, TriggerTypeCode.CHILD_SHIFT_D7, 100, today);
			childCondition = true;
		}

		if (today.equals(target.getChildBirthDate().plusYears(7))) {
			insertAction(target, TriggerTypeCode.CHILD_TURN_7, 75, today);
			childCondition = true;
		}

		if (today.equals(target.getChildBirthDate().plusYears(14))) {
			insertAction(target, TriggerTypeCode.CHILD_TURN_14, 75, today);
			childCondition = true;
		}

		return childCondition;
	}

	// 부모 조건(상령일, 나이 전환) 확인 및 액션 생성
	private boolean createParentActions(ActionTargetDto target, LocalDate today) {

		boolean parentCondition = false;

		LocalDate parentShiftDate =
				calculateThisYearShiftDate(target.getParentBirthDate(), today);

		if (today.equals(parentShiftDate.minusDays(30))) {
			insertAction(target, TriggerTypeCode.PARENT_SHIFT_D30, 80, today);
			parentCondition = true;
		}

		if (today.equals(parentShiftDate.minusDays(7))) {
			insertAction(target, TriggerTypeCode.PARENT_SHIFT_D7, 95, today);
			parentCondition = true;
		}

		if (today.equals(target.getParentBirthDate().plusYears(30))) {
			insertAction(target, TriggerTypeCode.PARENT_TURN_30, 70, today);
			parentCondition = true;
		}

		if (today.equals(target.getParentBirthDate().plusYears(50))) {
			insertAction(target, TriggerTypeCode.PARENT_TURN_50, 70, today);
			parentCondition = true;
		}

		if (today.equals(target.getParentBirthDate().plusYears(60))) {
			insertAction(target, TriggerTypeCode.PARENT_TURN_60, 70, today);
			parentCondition = true;
		}

		return parentCondition;
	}

	// 3-Step 조건에 따른 액션 생성
	private void createThreeStepActions(
			ActionTargetDto target,
			LocalDate today,
			boolean childCondition,
			boolean parentCondition
	) {
		if (childCondition && parentCondition) {
			insertAction(target, TriggerTypeCode.FAMILY_REMODELING, 90, today);
			return;
		}

		if (childCondition) {
			insertAction(target, TriggerTypeCode.CHILD_COVERAGE_CHECK, 65, today);
			return;
		}

		if (parentCondition) {
			insertAction(target, TriggerTypeCode.PARENT_HEALTH_CHECK, 60, today);
		}
	}

	// 액션 아이템 저장
	private void insertAction(
			ActionTargetDto target,
			String triggerTypeCode,
			int priorityScore,
			LocalDate targetDate
	) {
		log.info("액션 생성 customerId={}, triggerTypeCode={}, targetDate={}",
				target.getCustomerId(),
				triggerTypeCode,
				targetDate
		);

		dailyActionBatchMapper.insertDailyActionItem(
				target.getSalesUserId(),
				target.getCustomerId(),
				target.getConversionStatusCode(),
				triggerTypeCode,
				priorityScore,
				targetDate
		);
	}

	// 올해 기준 상령일 계산 (생일 + 6개월)
	private LocalDate calculateThisYearShiftDate(
			LocalDate birthDate,
			LocalDate today
	) {
		LocalDate shiftDate = birthDate
				.plusMonths(6)
				.withYear(today.getYear());

		if (shiftDate.isBefore(today)) {
			shiftDate = shiftDate.plusYears(1);
		}

		return shiftDate;
	}
}