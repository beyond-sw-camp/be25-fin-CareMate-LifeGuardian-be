package com.caremate.lifeguardian.userdetail.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("CustomerBadge")
public class CustomerBadgeRow {

    private String code;
    private String name;
}
