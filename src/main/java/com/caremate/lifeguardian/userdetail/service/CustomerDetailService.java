package com.caremate.lifeguardian.userdetail.service;

import com.caremate.lifeguardian.userdetail.dto.response.CustomerBasicInfoResponse;

public interface CustomerDetailService {

    CustomerBasicInfoResponse getCustomerBasicInfo(
            Long customerId,
            String conversionStatusCode,
            Long currentUserId
    );
}
