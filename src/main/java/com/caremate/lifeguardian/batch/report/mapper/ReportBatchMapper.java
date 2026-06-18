package com.caremate.lifeguardian.batch.report.mapper;

import com.caremate.lifeguardian.report.dto.internal.ActionItemInsertDto;
import com.caremate.lifeguardian.report.dto.internal.ReportTargetDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportBatchMapper {

    // 배치 자동 생성 대상 고객 목록 조회
    List<ReportTargetDto> selectReportTargets(@Param("reportYear") Integer reportYear);

    // 리포트 생성 사유 액션 아이템 저장
    int insertActionItem(ActionItemInsertDto actionItem);
}
