package com.caremate.lifeguardian.common.security;


import com.caremate.lifeguardian.common.exception.BaseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public final class SecurityUtil {
    private SecurityUtil(){
        throw  new AssertionError("인스턴스 생성 불가");
    }

    public static Long getCurrentUserId() {
        // 무조건 1000001번만 나오게 함
        return 1000003L;
    }
}
