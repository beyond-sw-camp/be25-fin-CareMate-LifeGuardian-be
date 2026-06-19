package com.caremate.lifeguardian.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TokenManagementMapper {

    /**
     * 특정 영업사원의 모든 활성 Refresh Token을 블랙리스트 처리합니다.
     */
    int blacklistTokensByUserId(@Param("salesUserId") Long salesUserId);
}
