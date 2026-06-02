package com.caremate.lifeguardian.member.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.dto.request.SalesUserRegisterRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserRegisterResponse;
import com.caremate.lifeguardian.member.mapper.BranchMapper;
import com.caremate.lifeguardian.member.mapper.SalesUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesUserService {

    private final SalesUserMapper salesUserMapper;
    private final BranchMapper branchMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$";
    private static final String ALLOWED_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final SecureRandom random = new SecureRandom();

    // 신입 영업사원을 등록, 사번과 최초 1회성 임시 비밀번호를 반환
    @Transactional
    public SalesUserRegisterResponse registerSalesUser(SalesUserRegisterRequest request) {
        log.info("신규 영업사원 등록 프로세스 시작 - 이름: {}, 지점 ID: {}", request.getName(), request.getBranchId());

        // 1. 소속 지점 존재 여부 검증
        if (!branchMapper.existsById(request.getBranchId())) {
            log.warn("지점 검증 실패 - 존재하지 않는 지점 ID: {}", request.getBranchId());
            throw new BaseException(404, "요청하신 지점 정보를 찾을 수 없습니다.");
        }

        // 2. 이메일 중복 검증
        if (salesUserMapper.existsByEmail(request.getEmail())) {
            log.warn("영업사원 등록 실패 - 이미 존재하는 이메일입니다: {}", request.getEmail());
            throw new BaseException(409, "이미 존재하는 이메일입니다. 기존 영업사원 정보를 확인해 주세요.");
        }

        // 3. 휴대폰 번호 중복 검증
        if (salesUserMapper.existsByPhone(request.getPhone())) {
            log.warn("영업사원 등록 실패 - 이미 존재하는 휴대폰 번호입니다: {}", request.getPhone());
            throw new BaseException(409, "이미 존재하는 휴대폰 번호입니다. 기존 영업사원 정보를 확인해 주세요.");
        }

        // 2. 임시 비밀번호 생성 (평문 8자리)
        String tempPassword = generateTemporaryPassword();

        // 3. 비밀번호 BCrypt 해싱 암호화
        String passwordHash = passwordEncoder.encode(tempPassword);

        // 4. 권한 코드 기본값 처리 ('02' 일반 영업사원)
        String roleCode = StringUtils.hasText(request.getRoleCode()) ? request.getRoleCode() : "02";

        // 5. UNIQUE 제약조건 우회를 위해 임시 사번 생성
        String tempEmployeeId = "TEMP-" + UUID.randomUUID().toString().substring(0, 15);

        // 6. 도메인 모델 생성 및 초기 강제 상태 주입
        SalesUser salesUser = SalesUser.builder()
                .branchId(request.getBranchId())
                .employeeId(tempEmployeeId) // 임시 사번 선 할당
                .passwordHash(passwordHash)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .phone(request.getPhone())
                .rankCode(request.getRankCode())
                .roleCode(roleCode)
                .statusCode("01") // 활성
                .isTempPassword(true) // 최초 비밀번호 변경 유도용
                .termsAgreed(false) // 최초 약관 동의 유도용
                .joinedAt(request.getJoinedAt())
                .build();

        // 7. DB Insert 실행 (Generated Keys 설정으로 PK가 salesUser.id에 세팅됨)
        salesUserMapper.insertSalesUser(salesUser);
        Long generatedId = salesUser.getId();
        log.info("영업사원 테이블 레코드 Insert 성공 - 생성된 PK: {}", generatedId);

        // 8. PK 값을 사번(employeeId)으로 변환 후 동기화 업데이트
        String finalEmployeeId = String.valueOf(generatedId);
        salesUser.assignEmployeeId(finalEmployeeId); // 객체 상태 변경 (No Setter 비즈니스 메서드 사용)
        salesUserMapper.updateEmployeeId(generatedId, finalEmployeeId); // DB 상태 동기화
        log.info("영업사원 사번 동기화 완료 - 사번: {}", finalEmployeeId);

        // 9. 불변 객체 Response 조립하여 반환
        return SalesUserRegisterResponse.builder()
                .id(generatedId)
                .employeeId(finalEmployeeId)
                .temporaryPassword(tempPassword) // 딱 한 번 평문으로 내려줌
                .build();
    }

    // 8자리의 안전한 임시 비밀번호를 무작위 생tjd
    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(8);

        // 안전한 난수를 위해 각 분류별 최소 1글자 보장
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // 나머지 4자리를 임의의 문자 조합으로 채움
        for (int i = 4; i < 8; i++) {
            password.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }

        // 완성된 비밀번호의 패턴 예측을 더 어렵게 하기 위해 섞어줌
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char a = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = a;
        }

        return new String(passwordArray);
    }
}
