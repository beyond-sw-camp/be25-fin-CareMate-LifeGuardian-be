package com.caremate.lifeguardian.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BranchStatisticsMapper {

    // 특정 지점의 연간 계약 수 카운트
    int countContractsByBranchAndYear(@Param("branchId") Long branchId, @Param("year") int year);

    // 특정 지점의 연간 목표치 조회 (소속 영업사원들의 월간 목표 합산)
    Integer selectAnnualTarget(@Param("branchId") Long branchId, @Param("year") int year);

    // 특정 지점의 월별 계약 수 카운트
    int countContractsByBranchAndMonth(@Param("branchId") Long branchId, @Param("yearMonth") String yearMonth);

    // 특정 지점의 활성 영업사원 수 조회
    int countActiveSalesUsersByBranch(@Param("branchId") Long branchId);

}
