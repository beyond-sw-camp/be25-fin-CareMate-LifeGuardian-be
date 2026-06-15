package com.caremate.lifeguardian.admin.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SalesUserMonthlyTrend {
    String month;
    int count;
}
