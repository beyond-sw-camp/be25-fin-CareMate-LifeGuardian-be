package com.caremate.lifeguardian.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesUserPerformance {
    private String employeeId;
    private String employeeName;
    private int contractCount;
    private int rank;
}
