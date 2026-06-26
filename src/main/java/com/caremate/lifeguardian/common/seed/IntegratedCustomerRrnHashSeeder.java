package com.caremate.lifeguardian.common.seed;

import com.caremate.lifeguardian.common.security.RrnHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class IntegratedCustomerRrnHashSeeder {

    private final RrnHashUtil rrnHashUtil;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public ApplicationRunner updateIntegratedCustomerRrnHash() {
        return args -> {
            jdbcTemplate.query("""
                SELECT integrated_customer_id, name, birth_date, gender
                FROM integrated_customer
                WHERE rrn_encrypted NOT REGEXP '^[0-9a-f]{64}$'
            """, rs -> {
                Long integratedCustomerId = rs.getLong("integrated_customer_id");
                String name = rs.getString("name");
                String birthDate = rs.getString("birth_date");
                String gender = rs.getString("gender");

                String rrnKey = buildRrnKey(birthDate, gender);
                String hashedRrn = rrnHashUtil.hash(rrnKey);

                int updatedCount = jdbcTemplate.update("""
                    UPDATE integrated_customer
                    SET rrn_encrypted = ?
                    WHERE integrated_customer_id = ?
                """, hashedRrn, integratedCustomerId);

                System.out.println(
                        "[RRN HASH SEED] "
                                + name
                                + " / ID="
                                + integratedCustomerId
                                + " / rrnKey="
                                + rrnKey
                                + " / updatedCount="
                                + updatedCount
                );
            });
        };
    }

    private String buildRrnKey(String birthDate, String gender) {
        String birth = birthDate.replace("-", "").substring(2, 8);
        String genderCode = resolveGenderCode(birthDate, gender);

        return birth + genderCode;
    }

    private String resolveGenderCode(String birthDate, String gender) {
        int birthYear = Integer.parseInt(birthDate.substring(0, 4));

        boolean isMale = "MALE".equalsIgnoreCase(gender)
                || "M".equalsIgnoreCase(gender)
                || "남".equals(gender);

        boolean isFemale = "FEMALE".equalsIgnoreCase(gender)
                || "F".equalsIgnoreCase(gender)
                || "여".equals(gender);

        if (birthYear >= 2000) {
            if (isMale) return "3";
            if (isFemale) return "4";
        }

        if (birthYear >= 1900) {
            if (isMale) return "1";
            if (isFemale) return "2";
        }

        throw new IllegalArgumentException(
                "성별 또는 생년월일로 주민번호 식별키를 만들 수 없습니다. birthDate="
                        + birthDate
                        + ", gender="
                        + gender
        );
    }
}