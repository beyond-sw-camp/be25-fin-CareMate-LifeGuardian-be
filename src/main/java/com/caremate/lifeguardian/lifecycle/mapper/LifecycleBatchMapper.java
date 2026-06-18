package com.caremate.lifeguardian.lifecycle.mapper;

import com.caremate.lifeguardian.lifecycle.dto.internal.LifecycleTargetDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LifecycleBatchMapper {

    List<LifecycleTargetDto> selectLifecycleTargets();

    int graduatePotentialCustomer(LifecycleTargetDto lifecycleTarget);
}
