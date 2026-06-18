package com.caremate.lifeguardian.scheduler.dailyaction.mapper;

import com.caremate.lifeguardian.scheduler.dailyaction.dto.ActionTargetDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyActionBatchMapper {

	List<ActionTargetDto> findActionTargets();

	void insertDailyActionItem(
			Long salesUserId,
			Long customerId,
			String conversionStatusCode,
			String triggerTypeCode,
			int priorityScore,
			LocalDate targetDate
	);
}