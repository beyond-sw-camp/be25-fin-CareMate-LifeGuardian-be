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

    // 특정 영업사원을 id로 조회
    SalesUser findById(@Param("id") Long id);

    // 영업사원의 계정 상태 업데이트
    int updateStatus(@Param("id") Long id, @Param("statusCode") String statusCode);

    // 영업사원의 잔여 고객(잠재 고객 + 통합 고객) 수의 총합 조회
    long countRemainingCustomers(@Param("userId") Long userId);

    // 원본 sales_user 테이블의 개인정보 데이터를 마스킹된 데이터로 덮어씌움
    int secureOriginalPii(
            @Param("id") Long id,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("birthDate") java.time.LocalDate birthDate);

    // 이관 이력 벌크 기록
    int insertCustomerAssignmentHistory(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId,
            @Param("changedByUserId") Long changedByUserId,
            @Param("reason") String reason);

    // 잠재고객 소유권 일괄 변경
    int updatePotentialCustomersUserId(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId);

    // 통합고객 소유권 일괄 변경
    int updateIntegratedCustomersUserId(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId);

}
