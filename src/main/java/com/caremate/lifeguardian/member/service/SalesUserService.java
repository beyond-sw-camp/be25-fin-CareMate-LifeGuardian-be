package com.caremate.lifeguardian.member.service;

import com.caremate.lifeguardian.member.dto.request.SalesUserRegisterRequest;
import com.caremate.lifeguardian.member.dto.request.SalesUserSearchRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserListResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserRegisterResponse;

public interface SalesUserService {
    // 신입 영업사원을 등록 자동사번 생성 및 임시 비밀번호 반환
    SalesUserRegisterResponse registerSalesUser(SalesUserRegisterRequest request);

    // 조건에 부합하는 영업사원 목록 페이징하여 조회
    SalesUserListResponse getSalesUserList(SalesUserSearchRequest searchRequest);
}
