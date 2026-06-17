package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerBasicInfoAlert {

    private String title;
    private String description;
    private String level;
}
