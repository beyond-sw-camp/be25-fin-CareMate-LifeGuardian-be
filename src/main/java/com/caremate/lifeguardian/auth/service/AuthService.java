package com.caremate.lifeguardian.auth.service;


import com.caremate.lifeguardian.auth.dto.request.LoginRequest;
import com.caremate.lifeguardian.auth.dto.response.LoginResponse;

public interface AuthService {

	LoginResponse login(
			LoginRequest request,
			String ipAddress,
			String userAgent
	);
}