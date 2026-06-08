package com.caremate.lifeguardian.member.mapper;

import com.caremate.lifeguardian.member.domain.SalesUserPiiSecure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SalesUserPiiSecureMapper {

    // 격리된 퇴사자 PII 보안 보관 테이블 삽입
    int insertPiiSecure(SalesUserPiiSecure piiSecure);

    // PII 보안 격리 보관 테이블의 전체 레코드 수 반환
    long countPiiSecure();

    // 페이징된 PII 보관 리스트 조회
    List<SalesUserPiiSecure> selectPiiSecureList(
            @Param("offset") int offset,
            @Param("size") int size);
}
