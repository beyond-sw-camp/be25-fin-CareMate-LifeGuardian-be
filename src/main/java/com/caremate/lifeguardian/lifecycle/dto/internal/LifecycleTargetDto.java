package com.caremate.lifeguardian.lifecycle.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("LifecycleTargetDto")
public class LifecycleTargetDto {

    private Long potentialCustomerId;
}
