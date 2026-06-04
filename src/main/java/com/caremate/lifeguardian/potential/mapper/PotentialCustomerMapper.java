package com.caremate.lifeguardian.potential.mapper;

import com.caremate.lifeguardian.potential.dto.request.ParentCustomerSearchRequest;
import com.caremate.lifeguardian.potential.dto.response.ParentCustomerSearchResponse;
import com.caremate.lifeguardian.potential.dto.response.PotentialCustomerListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PotentialCustomerMapper {

    // 잠재고객 목록 조회
    List<PotentialCustomerListResponse> findPotentialCustomersBySalesUserId(
            @Param("salesUserId") Long salesUserId
    );

    // 부모 통합고객 조회
    ParentCustomerSearchResponse findParentCustomer(
            @Param("request") ParentCustomerSearchRequest request,
            @Param("gender") String gender
    );
}
