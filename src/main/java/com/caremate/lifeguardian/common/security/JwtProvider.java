package com.caremate.lifeguardian.common.security;

import com.caremate.lifeguardian.common.exception.AuthException;
import com.caremate.lifeguardian.member.domain.enums.Role;
import com.caremate.lifeguardian.member.domain.enums.Status;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // OS 인코딩 환경에 영향받지 않도록 UTF-8 명시
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 메타데이터 정책(USER_ROLE, USER_STATUS 등)을 반영한 AccessToken 생성
     */
    public String createAccessToken(Long memberId, Role role, Status status) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(memberId.toString())
                // 메타테이블의 USER_ROLE (01: ADMIN, 02: SALES) 매핑
                .claim("role", role.name())
                .claim("status", status.name())
                // 필요시 메타테이블의 직급(USER_RANK)이나 상태(USER_STATUS) 추가 가능
                // .claim("status", "01") // 예: 01(활성)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * RefreshToken 생성 (보통 권한 정보는 제외하거나 최소화하여 페이로드 크기 단축)
     */
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 서명 및 만료 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
            throw new AuthException("유효하지 않은 토큰 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            throw new AuthException("토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            throw new AuthException("지원되지 않는 토큰 형식입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
            throw new AuthException("토큰이 비어있거나 잘못되었습니다.");
        }
    }

    /**
     * 토큰에서 Claims 전체 파싱
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 사용자 ID(Subject) 추출
     */
    public Long getMemberId(String token) {
        return Long.parseLong(
                getClaims(token).getSubject()
        );
    }

    /**
     * 토큰에서 사용자 권한(Role) 추출
     */
    public Role getRole(String token) {
        String roleStr = getClaims(token).get("role", String.class);
        return Role.valueOf(roleStr);
    }

    /**
     * Refresh 토큰 만료 일시 반환 (DB/Redis 저장용)
     */
    public LocalDateTime getRefreshTokenExpire() {
        return LocalDateTime.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS);
    }

    /**
     * Refresh 토큰 만료 시간(초) 반환 (Redis TTL 설정용)
     */
    public long getRefreshTokenStepSeconds() {
        return refreshTokenExpiration / 1000;
    }
}