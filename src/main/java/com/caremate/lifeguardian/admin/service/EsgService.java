package com.caremate.lifeguardian.admin.service;

import com.caremate.lifeguardian.admin.dto.response.EnvironmentalScoresResponse;
import com.caremate.lifeguardian.admin.dto.response.PeakCutProfileResponse;

public interface EsgService {

    // 환경(E) 지표 누적 스코어보드 조회
    EnvironmentalScoresResponse getEnvironmentalScores();

    // 24시간 인프라 부하 및 피크 컷 차트 조회
    PeakCutProfileResponse getPeakCutProfile(String targetDate);
}
