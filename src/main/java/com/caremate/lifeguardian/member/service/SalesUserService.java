package com.caremate.lifeguardian.member.service;

import com.caremate.lifeguardian.member.dto.request.*;
import com.caremate.lifeguardian.member.dto.response.*;

public interface SalesUserService {
    // 신입 영업사원을 등록 자동사번 생성 및 임시 비밀번호 반환
    SalesUserRegisterResponse registerSalesUser(SalesUserRegisterRequest request);

    // 조건에 부합하는 영업사원 목록 페이징하여 조회
    SalesUserListResponse getSalesUserList(SalesUserSearchRequest searchRequest);

    // 특정 영업사원의 계정 상태 변경, TODO 퇴사/정지인 경우 세션 무효화
    SalesUserStatusUpdateResponse changeSalesUserStatus(Long userId, SalesUserStatusUpdateRequest request);

    // 영업사원을 영구 퇴사 및 PII 및 TODO 기기 세션을 일괄 파기합니다.
    SalesUserRetireResponse retireSalesUser(Long userId);

    // 퇴사 예정자의 모든 잔여 고객을 다른 활성 영업사원에게 일괄 이관 및 이력 기록
    SalesUserCustomerTransferResponse transferCustomers(Long userId, SalesUserCustomerTransferRequest request, Long changedByUserId);

    // 분리 보관 중인 퇴사자 PII 보존 현황 페이징 조회
    SalesUserPiiSecureListResponse getPiiSecureList(SalesUserPiiSecureSearchRequest request);

}
