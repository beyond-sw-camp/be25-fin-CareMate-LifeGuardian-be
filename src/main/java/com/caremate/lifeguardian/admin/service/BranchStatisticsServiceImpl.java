package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.domain.SalesUserPerformance;
import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;
import com.caremate.lifeguardian.admin.dto.response.BranchMonthlyContractsResponse;
import com.caremate.lifeguardian.admin.dto.response.BranchSalesRankingResponse;
import com.caremate.lifeguardian.admin.dto.response.SalesPerformerInfo;
import com.caremate.lifeguardian.admin.mapper.BranchStatisticsMapper;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.member.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.caremate.lifeguardian.admin.domain.SalesUserMonthlyTrendDto;
import com.caremate.lifeguardian.admin.dto.response.SalesUserMonthlyTrend;
import com.caremate.lifeguardian.admin.dto.response.SalesUserPersonalPerformanceResponse;
import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.mapper.SalesUserMapper;
import com.caremate.lifeguardian.admin.domain.DashboardSalesUser;
import com.caremate.lifeguardian.admin.dto.response.DashboardSalesUserInfo;
import com.caremate.lifeguardian.admin.dto.response.DashboardSalesUsersResponse;
import com.caremate.lifeguardian.common.security.SecurityUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchStatisticsServiceImpl implements BranchStatisticsService {

    private final BranchMapper branchMapper;
    private final BranchStatisticsMapper branchStatisticsMapper;
    private final SalesUserMapper salesUserMapper;

    // 지점 연간 누적 계약 통계를 조회합니다.
    @Override
    @Cacheable(value = "branchAnnualContracts", key = "#branchId + '_' + (#targetYear != null ? #targetYear : T(java.time.LocalDate).now().getYear())")
    public BranchAnnualContractsResponse getBranchAnnualContracts(Long branchId, Integer targetYear) {
        // 1. 지점 존재 유무 검증
        if (!branchMapper.existsById(branchId)) {
            throw new BaseException(404, "요청하신 지점 정보를 찾을 수 없습니다.");
        }

        // 지점장 지점 권한 검증
        validateManagerBranch(branchId);

        // 2. targetYear 기본값 바인딩 및 유효성 검증
        int currentYear = LocalDate.now().getYear();
        if (targetYear == null) {
            targetYear = currentYear;
        } else if (targetYear > currentYear) {
            throw new BaseException(400, "올바른 연도 형식이 아닙니다.");
        }

        // 3. 지점의 올해 계약 수 및 전년 계약 수 조회
        int currentYearCount = branchStatisticsMapper.countContractsByBranchAndYear(branchId, targetYear);
        int previousYearCount = branchStatisticsMapper.countContractsByBranchAndYear(branchId, targetYear - 1);

        // 4. 지점의 해당 연도 연간 목표 수 조회 (목표가 없으면 0)
        Integer targetCount = branchStatisticsMapper.selectAnnualTarget(branchId, targetYear);
        int annualTargetCount = (targetCount != null) ? targetCount : 0;

        // 5. 비율 연산 및 Zero-Division 방어 로직 적용
        double yoyGrowthRate = 0.0;
        if (previousYearCount > 0) {
            double rawRate = ((currentYearCount - previousYearCount) / (double) previousYearCount) * 100.0;
            yoyGrowthRate = Math.round(rawRate * 10.0) / 10.0;
        }

        double targetAchievementRate = 0.0;
        if (annualTargetCount > 0) {
            double rawRate = (currentYearCount / (double) annualTargetCount) * 100.0;
            targetAchievementRate = Math.round(rawRate * 10.0) / 10.0;
        }

        // 6. Response DTO 빌드 후 반환
        return BranchAnnualContractsResponse.builder()
                .currentYearCount(currentYearCount)
                .previousYearCount(previousYearCount)
                .yoyGrowthRate(yoyGrowthRate)
                .annualTargetCount(annualTargetCount)
                .targetAchievementRate(targetAchievementRate)
                .build();
    }

    // 지점 월간 당월 계약 통계를 조회합니다.
    @Override
    @Cacheable(value = "branchMonthlyContracts", key = "#branchId + '_' + (#targetYearMonth != null ? #targetYearMonth : T(java.time.format.DateTimeFormatter).ofPattern('yyyy-MM').format(T(java.time.LocalDate).now()))")
    public BranchMonthlyContractsResponse getBranchMonthlyContracts(Long branchId, String targetYearMonth) {
        // 1. 지점 존재 유무 검증
        if (!branchMapper.existsById(branchId)) {
            throw new BaseException(404, "요청하신 지점 정보를 찾을 수 없습니다.");
        }

        // 지점장 지점 권한 검증
        validateManagerBranch(branchId);

        // 2. targetYearMonth 파싱 및 유효성 검증
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth parsedYearMonth;
        if (targetYearMonth == null) {
            parsedYearMonth = currentYearMonth;
            targetYearMonth = parsedYearMonth.toString(); // YYYY-MM
        } else {
            try {
                parsedYearMonth = YearMonth.parse(targetYearMonth);
            } catch (DateTimeParseException e) {
                throw new BaseException(400, "올바른 연월 형식(YYYY-MM)이 아닙니다.");
            }
            if (parsedYearMonth.isAfter(currentYearMonth)) {
                throw new BaseException(400, "올바른 연월 형식(YYYY-MM)이 아닙니다.");
            }
        }

        // 3. 지점의 당월 계약 수 및 전월 계약 수 조회
        int currentMonthCount = branchStatisticsMapper.countContractsByBranchAndMonth(branchId, targetYearMonth);

        String previousYearMonth = parsedYearMonth.minusMonths(1).toString(); // YYYY-MM
        int previousMonthCount = branchStatisticsMapper.countContractsByBranchAndMonth(branchId, previousYearMonth);

        // 4. 지점의 활성 영업사원 수 조회
        int activeSalesUserCount = branchStatisticsMapper.countActiveSalesUsersByBranch(branchId);

        // 5. 연산 및 Zero-Division 방어 로직 적용
        int momDifferenceCount = currentMonthCount - previousMonthCount;

        double averagePerUser = 0.0;
        if (activeSalesUserCount > 0) {
            double rawAverage = currentMonthCount / (double) activeSalesUserCount;
            averagePerUser = Math.round(rawAverage * 10.0) / 10.0;
        }

        // 6. Response DTO 빌드 후 반환
        return BranchMonthlyContractsResponse.builder()
                .currentMonthCount(currentMonthCount)
                .previousMonthCount(previousMonthCount)
                .momDifferenceCount(momDifferenceCount)
                .activeSalesUserCount(activeSalesUserCount)
                .averagePerUser(averagePerUser)
                .build();
    }

    // 지점 월간 판매 실적 상/하위 랭킹을 조회합니다.
    @Override
    @Cacheable(value = "branchSalesRanking", key = "#branchId + '_' + (#targetYearMonth != null ? #targetYearMonth : T(java.time.format.DateTimeFormatter).ofPattern('yyyy-MM').format(T(java.time.LocalDate).now()))")
    public BranchSalesRankingResponse getBranchSalesRanking(Long branchId, String targetYearMonth) {
        // 1. 지점 존재 유무 검증
        if (!branchMapper.existsById(branchId)) {
            throw new BaseException(404, "요청하신 지점 정보를 찾을 수 없습니다.");
        }

        // 지점장 지점 권한 검증
        validateManagerBranch(branchId);

        // 2. targetYearMonth 파싱 및 유효성 검증
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth parsedYearMonth;
        if (targetYearMonth == null) {
            parsedYearMonth = currentYearMonth;
            targetYearMonth = parsedYearMonth.toString(); // YYYY-MM
        } else {
            try {
                parsedYearMonth = YearMonth.parse(targetYearMonth);
            } catch (DateTimeParseException e) {
                throw new BaseException(400, "올바른 연월 형식(YYYY-MM)이 아닙니다.");
            }
            if (parsedYearMonth.isAfter(currentYearMonth)) {
                throw new BaseException(400, "올바른 연월 형식(YYYY-MM)이 아닙니다.");
            }
        }

        // 3. 지점 내 영업사원들의 계약 실적 랭킹 목록 조회
        List<SalesUserPerformance> allPerformers = branchStatisticsMapper.selectSalesUsersPerformanceRanking(branchId,
                targetYearMonth);

        // 4. 최소 인원(2명 이하) 제한 및 무실적(Empty State) 검증
        boolean isEmptyState = allPerformers.size() <= 2
                || allPerformers.get(0).getContractCount() == 0;

        if (isEmptyState) {
            return BranchSalesRankingResponse.builder()
                    .appliedYearMonth(targetYearMonth)
                    .topPerformers(new ArrayList<>())
                    .bottomPerformers(new ArrayList<>())
                    .build();
        }

        // 5. 상위 3명 추출
        List<SalesPerformerInfo> topPerformers = allPerformers.stream()
                .limit(3)
                .map(p -> SalesPerformerInfo.builder()
                        .rank(p.getRank())
                        .employeeId(p.getEmployeeId())
                        .employeeName(p.getEmployeeName())
                        .contractCount(p.getContractCount())
                        .build())
                .collect(Collectors.toList());

        // 6. 하위 3명 추출 (교집합 제거 및 오름차순 정렬)
        Set<String> topEmployeeIds = topPerformers.stream()
                .map(SalesPerformerInfo::getEmployeeId)
                .collect(Collectors.toSet());

        List<SalesUserPerformance> remaining = allPerformers.stream()
                .filter(p -> !topEmployeeIds.contains(p.getEmployeeId()))
                .toList();

        List<SalesPerformerInfo> bottomPerformers = new ArrayList<>();
        int remainingSize = remaining.size();
        if (remainingSize > 0) {
            int start = Math.max(0, remainingSize - 3);
            List<SalesUserPerformance> tempBottoms = remaining.subList(start, remainingSize);
            // 최하위 실적자(리스트 맨 뒤)부터 역순(Reversed)으로 정렬하여 추가 (가장 낮은 성적의 사원부터 먼저 오게끔)
            for (int i = tempBottoms.size() - 1; i >= 0; i--) {
                SalesUserPerformance p = tempBottoms.get(i);
                bottomPerformers.add(SalesPerformerInfo.builder()
                        .rank(p.getRank())
                        .employeeId(p.getEmployeeId())
                        .employeeName(p.getEmployeeName())
                        .contractCount(p.getContractCount())
                        .build());
            }
        }

        // 7. Response DTO 반환
        return BranchSalesRankingResponse.builder()
                .appliedYearMonth(targetYearMonth)
                .topPerformers(topPerformers)
                .bottomPerformers(bottomPerformers)
                .build();
    }

    // 영업사원 개인 판매 실적 상세 정보를 조회합니다.
    @Override
    public SalesUserPersonalPerformanceResponse getSalesUserPersonalPerformance(Long branchId, Long targetUserId) {
        // 1. 대상 영업사원 존재 검증
        SalesUser salesUser = salesUserMapper.findById(targetUserId);
        if (salesUser == null) {
            throw new BaseException(404, "요청하신 영업사원 정보를 찾을 수 없습니다.");
        }

        // 2. 지점 일치 검증
        if (!salesUser.getBranchId().equals(branchId)) {
            throw new BaseException(404, "요청하신 영업사원 정보를 찾을 수 없습니다.");
        }

        // 지점장 지점 권한 검증
        validateManagerBranch(branchId);

        // 3. 기준 일자 및 연월 계산
        LocalDate today = LocalDate.now();
        String currentYearMonth = YearMonth.from(today).toString(); // "YYYY-MM"
        String previousYearMonth = YearMonth.from(today).minusMonths(1).toString(); // "YYYY-MM"
        int currentYear = today.getYear();

        // 4. 당월 및 전월 실적, 연간 누적 실적 조회
        int thisMonthCount = branchStatisticsMapper.countSalesUserContractsByMonth(targetUserId, currentYearMonth);
        int previousMonthCount = branchStatisticsMapper.countSalesUserContractsByMonth(targetUserId, previousYearMonth);
        int annualCount = branchStatisticsMapper.countSalesUserContractsByYear(targetUserId, currentYear);

        // 5. 월간 목표 조회
        String targetYearMonthFormat = currentYearMonth.replace("-", ""); // "YYYYMM"
        Integer targetCount = branchStatisticsMapper.selectSalesUserMonthlyTarget(targetUserId, targetYearMonthFormat);
        int monthlyTargetCount = (targetCount != null) ? targetCount : 0;

        // 6. 목표 대비 차이값 계산
        int targetDifference = thisMonthCount - monthlyTargetCount;

        // 7. 전월 대비 증감 계산
        int momDifference = thisMonthCount - previousMonthCount;

        // 8. 목표 달성률 계산 (Zero-Division 방어)
        double targetAchievementRate = 0.0;
        if (monthlyTargetCount > 0) {
            double rawRate = (thisMonthCount / (double) monthlyTargetCount) * 100.0;
            targetAchievementRate = Math.round(rawRate * 10.0) / 10.0;
        }

        // 9. 목표 달성 여부 판별
        boolean isTargetAchieved = thisMonthCount >= monthlyTargetCount;

        // 10. 지점 내 활성 사원수 및 랭킹 조회
        int totalBranchUsers = branchStatisticsMapper.countActiveSalesUsersByBranch(branchId);
        Integer userRankVal = branchStatisticsMapper.selectSalesUserBranchRank(branchId, targetUserId, currentYearMonth);
        int branchRank = (userRankVal != null) ? userRankVal : 0;

        // 11. 직급 매핑
        String positionName = branchStatisticsMapper.selectPositionName(salesUser.getRankCode());
        if (positionName == null) {
            positionName = "일반 영업사원";
        }

        // 12. 6개월간의 시계열 데이터 조회 및 가공
        LocalDate startLocalDate = today.minusMonths(5).withDayOfMonth(1);
        String startDateTime = startLocalDate.atStartOfDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<SalesUserMonthlyTrendDto> trendDtos = branchStatisticsMapper.selectSalesUserMonthlyTrends(targetUserId, startDateTime);
        java.util.Map<String, Integer> trendMap = trendDtos.stream()
                .collect(Collectors.toMap(SalesUserMonthlyTrendDto::getYearMonth, SalesUserMonthlyTrendDto::getContractCount, (v1, v2) -> v1));

        List<SalesUserMonthlyTrend> monthlyTrends = new ArrayList<>();
        YearMonth startYM = YearMonth.from(startLocalDate);
        for (int i = 0; i < 6; i++) {
            YearMonth ym = startYM.plusMonths(i);
            String ymStr = ym.toString(); // "YYYY-MM"
            int count = trendMap.getOrDefault(ymStr, 0);
            String monthLabel = ym.getMonthValue() + "월";

            monthlyTrends.add(SalesUserMonthlyTrend.builder()
                    .month(monthLabel)
                    .count(count)
                    .build());
        }

        // 13. 응답 반환
        return SalesUserPersonalPerformanceResponse.builder()
                .employeeName(salesUser.getName())
                .positionName(positionName)
                .thisMonthCount(thisMonthCount)
                .annualCount(annualCount)
                .monthlyTargetCount(monthlyTargetCount)
                .targetDifference(targetDifference)
                .monthlyTrends(monthlyTrends)
                .previousMonthCount(previousMonthCount)
                .momDifference(momDifference)
                .targetAchievementRate(targetAchievementRate)
                .branchRank(branchRank)
                .totalBranchUsers(totalBranchUsers)
                .isTargetAchieved(isTargetAchieved)
                .build();
    }

    // 대시보드용 영업사원 목록을 조회합니다.
    @Override
    public DashboardSalesUsersResponse getDashboardSalesUsers(Long branchId, String keyword) {
        // 1. 지점 존재 유무 검증
        if (!branchMapper.existsById(branchId)) {
            throw new BaseException(404, "요청하신 지점 정보를 찾을 수 없습니다.");
        }

        // 지점장 지점 권한 검증
        validateManagerBranch(branchId);

        // 2. 현재 연월 계산 (format: YYYY-MM)
        String currentYearMonth = YearMonth.now().toString();

        // 3. 로그인한 지점장 ID 조회
        Long managerUserId = SecurityUtil.getCurrentUserId();

        // 4. Mapper 메서드 호출
        List<DashboardSalesUser> domainList = branchStatisticsMapper.selectDashboardSalesUsers(
                branchId, managerUserId, currentYearMonth, keyword
        );

        // 5. DTO 변환 및 카운트 집계
        int totalCount = domainList.size();
        int pinnedCount = 0;
        List<DashboardSalesUserInfo> dtoList = new ArrayList<>();

        for (DashboardSalesUser user : domainList) {
            if (user.isPinned()) {
                pinnedCount++;
            }
            dtoList.add(DashboardSalesUserInfo.builder()
                    .userId(user.getUserId())
                    .employeeName(user.getEmployeeName())
                    .rank(user.getRank())
                    .thisMonthCount(user.getThisMonthCount())
                    .targetDifference(user.getTargetDifference())
                    .isPinned(user.isPinned())
                    .build());
        }

        // 6. Response DTO 반환
        return DashboardSalesUsersResponse.builder()
                .totalCount(totalCount)
                .pinnedCount(pinnedCount)
                .salesUsers(dtoList)
                .build();
    }

    // 영업사원을 대시보드에 핀 고정합니다.
    @Override
    @Transactional
    public void pinSalesUser(Long targetUserId) {
        // 1. 대상 영업사원 존재 검증
        SalesUser targetUser = salesUserMapper.findById(targetUserId);
        if (targetUser == null) {
            throw new BaseException(404, "핀 설정을 변경할 대상 영업사원을 찾을 수 없습니다.");
        }

        // 2. 로그인한 지점장 ID 조회
        Long managerUserId = SecurityUtil.getCurrentUserId();

        // 3. 지점 일치 검증 (지점장과 영업사원의 소속 지점이 다를 경우 권한 차단)
        SalesUser manager = salesUserMapper.findById(managerUserId);
        if (manager == null || !manager.getBranchId().equals(targetUser.getBranchId())) {
            throw new BaseException(403, "해당 영업사원을 핀 고정할 권한이 없습니다.");
        }

        // 4. 중복 핀 검증
        if (branchStatisticsMapper.existsPin(managerUserId, targetUserId)) {
            throw new BaseException(409, "이미 핀 고정된 영업사원입니다.");
        }

        try {
            // 5. insert 실행
            branchStatisticsMapper.insertPin(managerUserId, targetUserId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 레이스 컨디션 방어
            throw new BaseException(409, "이미 핀 고정된 영업사원입니다.");
        }
    }

    // 영업사원의 대시보드 핀 고정을 해제합니다.
    @Override
    @Transactional
    public void unpinSalesUser(Long targetUserId) {
        // 1. 대상 영업사원 존재 검증
        SalesUser targetUser = salesUserMapper.findById(targetUserId);
        if (targetUser == null) {
            throw new BaseException(404, "핀 설정을 변경할 대상 영업사원을 찾을 수 없습니다.");
        }

        // 2. 로그인한 지점장 ID 조회
        Long managerUserId = SecurityUtil.getCurrentUserId();

        // 3. 지점 일치 검증 (지점장과 영업사원의 소속 지점이 다를 경우 권한 차단)
        SalesUser manager = salesUserMapper.findById(managerUserId);
        if (manager == null || !manager.getBranchId().equals(targetUser.getBranchId())) {
            throw new BaseException(403, "해당 영업사원의 핀 설정을 변경할 권한이 없습니다.");
        }

        // 4. delete 실행 (고정되어 있지 않은 경우에도 멱등적으로 성공 반환)
        branchStatisticsMapper.deletePin(managerUserId, targetUserId);
    }

    private void validateManagerBranch(Long branchId) {
        Long managerUserId = SecurityUtil.getCurrentUserId();
        SalesUser manager = salesUserMapper.findById(managerUserId);
        if (manager == null || !manager.getBranchId().equals(branchId)) {
            throw new BaseException(403, "해당 지점의 데이터를 조회할 권한이 없습니다.");
        }
    }
}
