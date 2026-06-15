package com.caremate.lifeguardian.admin.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class DashboardSalesUsersResponse {
    int totalCount;
    int pinnedCount;
    List<DashboardSalesUserInfo> salesUsers;
}
