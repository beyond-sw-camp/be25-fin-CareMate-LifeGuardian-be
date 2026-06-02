package com.caremate.lifeguardian.member.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BranchMapper {
    /**
     * 특정 지점이 DB에 존재하는지 여부를 확인합니다.
     */
    boolean existsById(Long id);
}
