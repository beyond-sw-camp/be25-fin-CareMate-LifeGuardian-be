package com.caremate.lifeguardian.admin.dto.response;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class PeakCutProfileResponse {
    String targetDate;
    List<HourlyProfileDto> hourlyProfiles;

    @Value
    @Builder
    public static class HourlyProfileDto {
        String hour;
        double traditionalLoad;
        double optimizedLoad;
    }
}
