package com.caremate.lifeguardian.common.security;

import com.caremate.lifeguardian.common.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class RrnHashUtil {

    @Value("${app.security.rrn-pepper}")
    private String rrnPepper;

    /**
     * 주민번호 식별키를 해시 처리한다.
     *
     * 입력 예:
     * - 830411-1******
     * -8304111
     *
     * 정규화 결과:
     * - 8304111
     */
    public String hash(String rrn) {
        String rrnKey = normalizeToRrnKey(rrn);
        String valueToHash = rrnKey + rrnPepper;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(valueToHash.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    result.append('0');
                }

                result.append(hex);
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("주민번호 해시 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 주민번호 입력값을 비교용 식별키로 변환한다.
     *
     * 허용 입력:
     * - 830411-1******
     * - 8304111
     *
     * 반환:
     * - 8304111
     */
    private String normalizeToRrnKey(String rrn) {
        if (rrn == null || rrn.isBlank()) {
            throw new BaseException(400, "주민번호 식별값은 필수입니다.");
        }

        String normalized = rrn.trim()
                .replace("-", "")
                .replace("*", "");

        if (!normalized.matches("\\d{7}")) {
            throw new BaseException(400, "주민번호는 생년월일 6자리와 뒤 첫 자리 형식이어야 합니다.");
        }

        return normalized;
    }
}
