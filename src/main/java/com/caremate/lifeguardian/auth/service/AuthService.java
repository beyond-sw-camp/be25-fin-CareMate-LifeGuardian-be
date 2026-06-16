package com.caremate.lifeguardian.auth.service;


import com.caremate.lifeguardian.auth.dto.request.InitialPasswordResetRequest;
import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.response.AuthResultDto;

public interface AuthService {

	AuthResultDto login(
			LoginRequest request,
			String ipAddress,
			String userAgent
	);

	void resetInitialPassword(
			Long userId,
			InitialPasswordResetRequest request,
			String ipAddress,
			String userAgent
	);
}