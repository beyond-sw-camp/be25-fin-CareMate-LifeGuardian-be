package com.caremate.lifeguardian.script.mapper;

import com.caremate.lifeguardian.script.dto.ScriptContextDto;
import com.caremate.lifeguardian.script.dto.response.CustomerScriptResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerScriptMapper {

	Long findTodayActionItemIdByCustomerId(Long customerId);

	CustomerScriptResponse findScriptByActionItemId(Long actionItemId);

	ScriptContextDto findScriptContext(Long actionItemId);

	void insertCustomerScript(
			@Param("customerId") Long customerId,
			@Param("conversionStatusCode") String conversionStatusCode,
			@Param("actionItemId") Long actionItemId,
			@Param("scriptContent") String scriptContent
	);

	boolean existsCustomerBySalesUserId(
			@Param("salesUserId") Long salesUserId,
			@Param("customerId") Long customerId
	);
}