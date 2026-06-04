package com.caremate.lifeguardian.member.service;

import com.caremate.lifeguardian.member.dto.request.SalesUserRegisterRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserRegisterResponse;

public interface SalesUserService {
    SalesUserRegisterResponse registerSalesUser(SalesUserRegisterRequest request);
}
