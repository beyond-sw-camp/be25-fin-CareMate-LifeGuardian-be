package com.caremate.lifeguardian.batch.lifecycle.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LifecycleBatchMapper {

    int graduatePotentialCustomers();
}
