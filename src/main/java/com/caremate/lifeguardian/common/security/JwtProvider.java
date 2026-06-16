package com.caremate.lifeguardian.common.security;

import com.caremate.lifeguardian.common.exception.AuthException;
import com.caremate.lifeguardian.member.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

    // 사용자의 ID와 권한 정보를 담은 단기 Access Token 생성
    public String createAccessToken(Long memberId, Role role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(memberId.toString())
                .claim("role", role.name())
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

    // 토큰에서 Claims 전체 파싱
    // 토큰 파싱할떄 유효성 검증 통합
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new AuthException("유효하지 않은 토큰 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new AuthException("토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (UnsupportedJwtException e) {
            throw new AuthException("지원되지 않는 토큰 형식입니다.");
        } catch (IllegalArgumentException e) {
            throw new AuthException("토큰이 비어있거나 잘못되었습니다.");
        }
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