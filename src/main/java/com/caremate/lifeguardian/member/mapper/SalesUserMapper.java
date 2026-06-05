package com.caremate.lifeguardian.member.mapper;

import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.dto.request.SalesUserSearchRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalesUserMapper {
    //신규 영업사원 등록
    //keyProperty="id"를 통해 Insert 완료 후 PK 값이 user 객체에 바인딩
    int insertSalesUser(SalesUser user);

    // 사번(employeeId) 업데이트
    int updateEmployeeId(@Param("id") Long id, @Param("employeeId") String employeeId);

    // 특정 이메일을 사용하는 영업사원이 이미 존재하는지 여부 확인
    boolean existsByEmail(String email);

    // 특정 휴대폰 번호를 사용하는 영업사원이 이미 존재하는지 여부 확인
    boolean existsByPhone(String phone);

    // 조건에 해당하는 페이징된 영업사원 목록 정보 조회
    List<SalesUserInfo> selectSalesUserList(SalesUserSearchRequest searchRequest);

    // 조건에 해당하는 전체 영업사원 데이터 개수 조회
    long countSalesUsers(SalesUserSearchRequest searchRequest);


}
