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

    // 서버에서 로그인 사용자 ID로 덮어쓰는 값이다.
    private Long salesUserId;

    // 고객명 부분 검색 조건
    private String customerName;

    // 만 나이 검색 조건
    private Integer age;

    // 성별 검색 조건: Male, Female
    private String gender;

    // 계약 상태 코드 다중 선택 조건
    private List<String> contractStatusCodes;

    // 리포트 존재 여부 필터
    private Boolean hasReport;

    // 미완료 3Step 존재 여부 필터
    private Boolean hasThreeStep;


    // 페이지 번호는 1부터 시작한다.
    private int page = 1;

    // 한 페이지당 조회 건수
    private int size = 10;

    // MyBatis LIMIT/OFFSET 페이징에 사용할 시작 위치
    public int getOffset() {
        return (page-1)*size;
    }

}
