package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerBasicInfoBadge {

    private String code;
    private String name;
}
