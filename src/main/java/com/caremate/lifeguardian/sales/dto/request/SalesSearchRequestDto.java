package com.caremate.lifeguardian.sales.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
- 영업현황 목록 조회
- page, size, getOffset
 */
@Getter
@Setter
public class SalesSearchRequestDto {

    private Long salesUserId;

    private String customerName;

    private Integer age;

    private String gender;

    private List<String> contractStatusCodes;

    private Boolean hasReport;

    private Boolean hasThreeStep;


    private int page = 1;

    private int size = 10;

    public int getOffset() {
        return (page-1)*size;
    }

}
