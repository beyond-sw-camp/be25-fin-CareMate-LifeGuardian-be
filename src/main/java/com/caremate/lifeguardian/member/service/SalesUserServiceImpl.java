package com.caremate.lifeguardian.member.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.exception.RemainingCustomerConflictException;
import com.caremate.lifeguardian.common.redis.RedisKeyGenerator;
import com.caremate.lifeguardian.common.redis.RedisService;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.domain.SalesUserPiiSecure;
import com.caremate.lifeguardian.member.dto.request.SalesUserCustomerTransferRequest;
import com.caremate.lifeguardian.member.dto.request.SalesUserPiiSecureSearchRequest;
import com.caremate.lifeguardian.member.dto.request.SalesUserRegisterRequest;
import com.caremate.lifeguardian.member.dto.request.SalesUserSearchRequest;
import com.caremate.lifeguardian.member.dto.request.SalesUserStatusUpdateRequest;
import com.caremate.lifeguardian.member.dto.response.SalesUserCustomerTransferResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserInfo;
import com.caremate.lifeguardian.member.dto.response.SalesUserListResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserPiiSecureInfo;
import com.caremate.lifeguardian.member.dto.response.SalesUserPiiSecureListResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserRegisterResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserRetireResponse;
import com.caremate.lifeguardian.member.dto.response.SalesUserStatusUpdateResponse;
import com.caremate.lifeguardian.member.mapper.BranchMapper;
import com.caremate.lifeguardian.member.mapper.SalesUserMapper;
import com.caremate.lifeguardian.member.mapper.SalesUserPiiSecureMapper;
import com.caremate.lifeguardian.member.mapper.TokenManagementMapper;
import com.caremate.lifeguardian.member.service.SalesUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesUserServiceImpl implements SalesUserService {

    private final SalesUserMapper salesUserMapper;
    private final BranchMapper branchMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenManagementMapper tokenManagementMapper;
    private final RedisService redisService;
    private final SalesUserPiiSecureMapper salesUserPiiSecureMapper;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$";
    private static final String ALLOWED_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 신입 영업사원을 등록하고 자동 채번된 사번과 최초 1회성 임시 비밀번호를 반환합니다.
     */
    @Override
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

    /**
     * 영문 대소문자, 숫자, 특수문자가 1자 이상 포함된 8자리의 안전한 임시 비밀번호를 무작위로 생성합니다.
     */
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

    /**
     * 조건에 부합하는 영업사원 목록을 페이징하여 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public SalesUserListResponse getSalesUserList(SalesUserSearchRequest searchRequest) {
        log.info("영업사원 목록 조회 요청 - keyword: {}, statusCode: {}, page: {}, size: {}",
                searchRequest.getKeyword(), searchRequest.getStatusCode(), searchRequest.getPage(),
                searchRequest.getSize());

        // 1. 전체 데이터 개수 카운트
        long totalElements = salesUserMapper.countSalesUsers(searchRequest);

        // 2. 전체 페이지 수 계산
        int size = searchRequest.getSafeSize();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 3. 데이터가 존재하지 않는 경우 가벼운 빈 리스트 반환
        if (totalElements == 0) {
            return SalesUserListResponse.builder()
                    .totalElements(0L)
                    .totalPages(0)
                    .content(java.util.Collections.emptyList())
                    .build();
        }

        // 4. 페이징 데이터 목록 조회 (N+1 성능 최적화 완료)
        List<SalesUserInfo> content = salesUserMapper.selectSalesUserList(searchRequest);

        // 5. 불변 Response DTO 조립 반환
        return SalesUserListResponse.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .content(content)
                .build();
    }

    /**
     * 특정 영업사원의 계정 상태를 변경하고, 퇴사/정지인 경우 세션을 무효화합니다.
     */
    @Override
    @Transactional
    public SalesUserStatusUpdateResponse changeSalesUserStatus(Long userId, SalesUserStatusUpdateRequest request) {
        log.info("영업사원 상태 변경 요청 - userId: {}, statusCode: {}", userId, request.getStatusCode());

        // 1. 대상 영업사원 존재 여부 검증
        SalesUser salesUser = salesUserMapper.findById(userId);
        if (salesUser == null) {
            log.warn("상태 변경 실패 - 존재하지 않는 영업사원 ID: {}", userId);
            throw new BaseException(404, "요청하신 영업사원 정보를 찾을 수 없습니다.");
        }

        String newStatusCode = request.getStatusCode();

        // 2. 이미 동일한 계정 상태인 경우 중복 변경 차단
        if (salesUser.getStatusCode().equals(newStatusCode)) {
            if ("02".equals(newStatusCode)) {
                log.warn("상태 변경 거부 - 이미 퇴사/정지 처리된 사원입니다. userId: {}", userId);
                throw new BaseException(400, "이미 퇴사/정지 처리된 영업사원입니다.");
            } else {
                log.warn("상태 변경 거부 - 이미 활성화 상태인 사원입니다. userId: {}", userId);
                throw new BaseException(400, "이미 활성화 상태인 영업사원입니다.");
            }
        }

        // 3. 비활성('02') 퇴사 처리 시 잔여 고객 검증
        if ("02".equals(newStatusCode)) {
            long remainingCount = salesUserMapper.countRemainingCustomers(userId);
            if (remainingCount > 0) {
                log.warn("퇴사 처리 불가 - 잔여 고객 존재: {}명, userId: {}", remainingCount, userId);
                throw new RemainingCustomerConflictException(remainingCount);
            }
        }

        // 3. 도메인 객체 상태 변경 및 DB 동기화
        salesUser.changeStatus(newStatusCode);
        salesUserMapper.updateStatus(userId, newStatusCode);
        log.info("영업사원 계정 상태 업데이트 완료 - userId: {}, statusCode: {}", userId, newStatusCode);

        // 4. 퇴사/정지('02') 상태로 정상 전이 시 즉각 세션 파기
        if ("02".equals(newStatusCode)) {
            // 4-1. DB 내 Refresh Token 만료 처리 (블랙리스트)
            int invalidatedCount = tokenManagementMapper.blacklistTokensByUserId(userId);
            log.info("DB Refresh Token 무효화 처리 완료 - 대상 유저 ID: {}, 무효화 건수: {}", userId, invalidatedCount);

            // 4-2. Redis 캐시 내 Refresh Token 삭제
            String redisKey = RedisKeyGenerator.refreshToken(userId);
            if (redisService.exists(redisKey)) {
                redisService.delete(redisKey);
                log.info("Redis Refresh Token 캐시 파기 완료 - Key: {}", redisKey);
            }
        }

        // 5. 공통 코드 매핑에 맞춰 statusName 제공 (01 -> 활성(ACTIVE), 02 -> 퇴사/정지(RETIRED))
        String statusName = "01".equals(newStatusCode) ? "활성(ACTIVE)" : "퇴사/정지(RETIRED)";

        return SalesUserStatusUpdateResponse.builder()
                .id(userId)
                .statusCode(newStatusCode)
                .statusName(statusName)
                .build();
    }

    /**
     * 특정 영업사원을 영구 퇴사 처리하고, 개인정보 격리(PII Secure) 및 기기 세션을 일괄 파기합니다.
     */
    @Override
    @Transactional
    public SalesUserRetireResponse retireSalesUser(Long userId) {
        log.info("퇴사자 비활성화 및 세션 파기 요청 수신 - userId: {}", userId);

        // 1. 대상 영업사원 존재 여부 검증
        SalesUser salesUser = salesUserMapper.findById(userId);
        if (salesUser == null) {
            log.warn("퇴사 처리 실패 - 존재하지 않는 영업사원 ID: {}", userId);
            throw new BaseException(404, "요청하신 영업사원 정보를 찾을 수 없습니다.");
        }

        // 2. 이미 퇴사('02') 상태인지 검증 (중복 퇴사 차단)
        if ("02".equals(salesUser.getStatusCode())) {
            log.warn("퇴사 처리 실패 - 이미 퇴사 처리된 사원입니다. userId: {}", userId);
            throw new BaseException(400, "이미 퇴사/정지 처리된 영업사원입니다.");
        }

        // 3. 잔여 고객 검증 (1명이라도 존재 시 퇴사 전면 차단)
        long remainingCount = salesUserMapper.countRemainingCustomers(userId);
        if (remainingCount > 0) {
            log.warn("퇴사 처리 차단 - 잔여 고객 존재: {}명, userId: {}", remainingCount, userId);
            throw new BaseException(409, "잔여 고객이 존재하여 퇴사 처리가 불가합니다. 고객 이관을 먼저 완료해주세요.");
        }

        // 4. PII 데이터 보안 격리 처리 (3년 법정 의무 보관)
        LocalDateTime retiredAt = LocalDateTime.now();
        LocalDateTime purgedAt = retiredAt.plusYears(3);

        SalesUserPiiSecure piiSecure = SalesUserPiiSecure.builder()
                .employeeId(salesUser.getEmployeeId())
                .phone(salesUser.getPhone())
                .email(salesUser.getEmail())
                .birthDate(salesUser.getBirthDate())
                .retiredAt(retiredAt)
                .purgedAt(purgedAt)
                .build();

        // 4-1. pii 격리 보안 보관 테이블 적재 (INSERT)
        salesUserPiiSecureMapper.insertPiiSecure(piiSecure);
        log.info("퇴사자 PII 보안 격리 테이블 이관 완료 - 사번: {}", salesUser.getEmployeeId());

        // 4-2. 원본 sales_user 테이블 내 PII 데이터 소프트 마스킹 처리 (NOT NULL 제약조건 우회)
        String maskedPhone = "000-0000-0000";
        String maskedEmail = "retired_" + userId + "@company.com";
        java.time.LocalDate maskedBirthDate = java.time.LocalDate.of(1900, 1, 1);

        salesUserMapper.secureOriginalPii(userId, maskedPhone, maskedEmail, maskedBirthDate);
        log.info("원본 테이블 내 PII 소프트 마스킹 완료 - userId: {}", userId);

        // 5. 사원 상태 코드 변경 ('02' 퇴사)
        salesUser.changeStatus("02");
        salesUserMapper.updateStatus(userId, "02");
        log.info("영업사원 계정 상태 퇴사('02') 전이 완료 - userId: {}", userId);

        // 6. 세션 및 토큰 만료 처리 (DB & Redis)
        // 6-1. DB Refresh Token 블랙리스트 무효화 및 만료 개수 리턴
        int invalidatedCount = tokenManagementMapper.blacklistTokensByUserId(userId);
        log.info("DB 내 Refresh Token 블랙리스트 업데이트 완료 - userId: {}, 무효화 건수: {}", userId, invalidatedCount);

        // 6-2. Redis 내 Refresh Token 캐시 파기
        String redisKey = RedisKeyGenerator.refreshToken(userId);
        if (redisService.exists(redisKey)) {
            redisService.delete(redisKey);
            log.info("Redis Refresh Token 캐시 강제 삭제 완료 - Key: {}", redisKey);
        }

        // 7. Response DTO 조립 반환 (retiredAt 포맷팅: yyyy-MM-dd HH:mm:ss)
        String formattedRetiredAt = retiredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return SalesUserRetireResponse.builder()
                .id(userId)
                .statusCode("02")
                .statusName("퇴사")
                .invalidatedTokenCount(invalidatedCount)
                .retiredAt(formattedRetiredAt)
                .build();
    }

    /**
     * 퇴사 예정자(또는 부서 이동자)의 모든 잔여 고객을 다른 활성 영업사원에게 일괄 이관하고 이력을 기록합니다.
     */
    @Override
    @Transactional
    public SalesUserCustomerTransferResponse transferCustomers(Long userId, SalesUserCustomerTransferRequest request,
                                                               Long changedByUserId) {
        Long toUserId = request.getToUserId();
        log.info("퇴사자 고객 일괄 이관 시작 - fromUserId: {}, toUserId: {}, 실행자 ID: {}", userId, toUserId, changedByUserId);

        // 1. 기존 영업사원 존재 여부 검증
        SalesUser fromUser = salesUserMapper.findById(userId);
        if (fromUser == null) {
            log.warn("이관 실패 - 존재하지 않는 기존 영업사원 ID: {}", userId);
            throw new BaseException(404, "요청하신 영업사원 정보를 찾을 수 없습니다.");
        }

        // 2. 새 담당 영업사원 존재 및 활성 상태 검증
        SalesUser toUser = salesUserMapper.findById(toUserId);
        if (toUser == null || !"01".equals(toUser.getStatusCode())) {
            log.warn("이관 실패 - 새 담당자를 찾을 수 없거나 활성 상태가 아님. toUserId: {}", toUserId);
            throw new BaseException(404, "고객을 인계받을 대상 영업사원을 찾을 수 없거나 이미 퇴사한 계정입니다.");
        }

        // 3. 기존 담당자와 새 담당자가 동일한지 검증
        if (userId.equals(toUserId)) {
            log.warn("이관 실패 - 기존 담당자와 새 담당자가 동일함. userId: {}", userId);
            throw new BaseException(400, "잘못된 요청입니다. 기존 담당자와 새 담당자가 동일할 수 없습니다.");
        }

        // 4. 기존 담당자의 잔여 고객 수 검증
        long remainingCustomers = salesUserMapper.countRemainingCustomers(userId);
        if (remainingCustomers == 0) {
            log.warn("이관 실패 - 배정된 잔여 고객이 없음. userId: {}", userId);
            throw new BaseException(409, "해당 영업사원에게 배정된 잔여 고객이 없습니다.");
        }

        // 5. 이관 이력 기록
        salesUserMapper.insertCustomerAssignmentHistory(userId, toUserId, changedByUserId, "퇴사자 고객 DB 이관");
        log.info("고객 이관 이력 저장 완료 - fromUserId: {}, toUserId: {}", userId, toUserId);

        // 6. 잠재 고객 소유권 일괄 변경
        int transferredPotentialCount = salesUserMapper.updatePotentialCustomersUserId(userId, toUserId);
        log.info("잠재 고객 소유권 변경 완료 - 건수: {}", transferredPotentialCount);

        // 7. 통합 고객 소유권 일괄 변경
        int transferredIntegratedCount = salesUserMapper.updateIntegratedCustomersUserId(userId, toUserId);
        log.info("통합 고객 소유권 변경 완료 - 건수: {}", transferredIntegratedCount);

        return SalesUserCustomerTransferResponse.builder()
                .fromUserId(userId)
                .toUserId(toUserId)
                .transferredPotentialCount(transferredPotentialCount)
                .transferredIntegratedCount(transferredIntegratedCount)
                .build();
    }


    /**
     * 분리 보관 중인 퇴사자 PII 보존 현황을 페이징 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public SalesUserPiiSecureListResponse getPiiSecureList(SalesUserPiiSecureSearchRequest request) {
        log.info("퇴사자 PII 분리 보관 현황 조회 - page: {}, size: {}", request.getPage(), request.getSize());

        long totalElements = salesUserPiiSecureMapper.countPiiSecure();
        int size = request.getSafeSize();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        if (totalElements == 0) {
            return SalesUserPiiSecureListResponse.builder()
                    .totalElements(0L)
                    .totalPages(0)
                    .content(java.util.Collections.emptyList())
                    .build();
        }

        List<SalesUserPiiSecure> list = salesUserPiiSecureMapper.selectPiiSecureList(request.getOffset(), size);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        List<SalesUserPiiSecureInfo> content = list.stream().map(pii -> {
            String retiredDateStr = pii.getRetiredAt().format(dateFormatter);
            String purgedDateStr = pii.getPurgedAt().format(dateFormatter);
            long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(today, pii.getPurgedAt().toLocalDate());

            return SalesUserPiiSecureInfo.builder()
                    .employeeId(pii.getEmployeeId())
                    .retiredAt(retiredDateStr)
                    .purgedAt(purgedDateStr)
                    .remainingDays(remainingDays)
                    .statusName("보관중")
                    .build();
        }).collect(java.util.stream.Collectors.toList());

        return SalesUserPiiSecureListResponse.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .content(content)
                .build();
    }
}
