package com.caremate.lifeguardian.userdetail.mapper;

import com.caremate.lifeguardian.userdetail.dto.response.CustomerBasicInfoRow;
import com.caremate.lifeguardian.userdetail.dto.response.CustomerBadgeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerDetailMapper {

    boolean existsCustomer(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode
    );

    CustomerBasicInfoRow selectPotentialCustomerBasicInfo(
            @Param("customerId") Long customerId,
            @Param("currentUserId") Long currentUserId
    );

    CustomerBasicInfoRow selectIntegratedCustomerBasicInfo(
            @Param("customerId") Long customerId,
            @Param("currentUserId") Long currentUserId
    );

    String selectLatestReportUrl(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode
    );

    List<CustomerBadgeRow> selectCustomerBadges(
            @Param("customerId") Long customerId,
            @Param("conversionStatusCode") String conversionStatusCode,
            @Param("currentUserId") Long currentUserId
    );
}
