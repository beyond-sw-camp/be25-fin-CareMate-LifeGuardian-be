package com.caremate.lifeguardian.script.service;

import com.caremate.lifeguardian.script.dto.response.CustomerScriptResponse;

public interface CustomerScriptService {

	CustomerScriptResponse getOrCreateTodayScript(
			Long salesUserId,
			Long customerId
	);
}