package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.response.BranchAnnualContractsResponse;
import com.caremate.lifeguardian.admin.mapper.BranchStatisticsMapper;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.member.dto.response.BranchMonthlyContractsResponse;
import com.caremate.lifeguardian.member.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import com.caremate.lifeguardian.member.domain.SalesUser;
import com.caremate.lifeguardian.member.mapper.SalesUserMapper;
import com.caremate.lifeguardian.common.security.SecurityUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchStatisticsServiceImpl implements BranchStatisticsService {

    private final BranchMapper branchMapper;
    private final BranchStatisticsMapper branchStatisticsMapper;
    private final SalesUserMapper salesUserMapper;

    // 지점 연간 누적 계약 통계 조회
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

    // 지점장 지점 권한 검증
    private void validateManagerBranch(Long branchId) {
        Long managerUserId = SecurityUtil.getCurrentUserId();
        SalesUser manager = salesUserMapper.findById(managerUserId);
        if (manager == null || !manager.getBranchId().equals(branchId)) {
            throw new BaseException(403, "해당 지점의 데이터를 조회할 권한이 없습니다.");
        }
    }

    // 지점 월간 당월 계약 통계 조회
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
}
