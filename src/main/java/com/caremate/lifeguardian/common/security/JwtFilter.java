package com.caremate.lifeguardian.common.security;

import com.caremate.lifeguardian.common.exception.AuthException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String requestURI = request.getRequestURI();

            if (requestURI.equals("/api/v1/auth/reissue")
                    || requestURI.equals("/api/v1/auth/login")) {
                filterChain.doFilter(request, response);
                return;
            }
            // Authorization 헤더 조회
            String header = request.getHeader("Authorization");

            // JWT가 없으면 다음 필터로 이동
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // "Bearer " 제거 후 토큰 추출
            String token = header.substring(7);

            // JWT Claims 조회
            Claims claims = jwtProvider.getClaims(token);

            Long memberId = Long.parseLong(claims.getSubject());
            String roleName = claims.get("role", String.class);

            // 권한 생성
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + roleName);

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            memberId,
                            null,
                            Collections.singleton(authority)
                    );

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);


            // 다음 필터 실행
            filterChain.doFilter(request, response);

        } catch (AuthException e) {

            // JWT 인증 예외 처리
            log.error("JWT 인증 실패: {}", e.getMessage());

            setErrorResponse(response, e.getMessage());
        }
    }

    /**
     * 인증 실패 시 401 응답 반환
     */
    private void setErrorResponse(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String json = String.format(
                "{\"status\":401,\"message\":\"%s\",\"data\":null}",
                message
        );

        response.getWriter().write(json);
    }
}