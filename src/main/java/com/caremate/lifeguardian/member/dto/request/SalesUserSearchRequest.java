package com.caremate.lifeguardian.member.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesUserSearchRequest {

    private String keyword;
    private String statusCode;
    private Integer page;
    private Integer size;

    //오프셋 값을 반환
    //page 또는 size가 지정되지 않았을 때의 null-safe 기본값 연산
    public int getOffset() {
        int targetPage = (this.page == null || this.page < 1) ? 1 : this.page;
        int targetSize = (this.size == null || this.size < 1) ? 10 : this.size;
        return (targetPage - 1) * targetSize;
    }

    // size 값을 반환
    public int getSafeSize() {
        return (this.size == null || this.size < 1) ? 10 : this.size;
    }
}
