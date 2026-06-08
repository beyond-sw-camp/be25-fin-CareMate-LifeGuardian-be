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
public class SalesUserPiiSecureSearchRequest {

    private Integer page;
    private Integer size;

    public int getOffset() {
        int targetPage = (this.page == null || this.page < 1) ? 1 : this.page;
        int targetSize = (this.size == null || this.size < 1) ? 10 : this.size;
        return (targetPage - 1) * targetSize;
    }

    public int getSafeSize() {
        return (this.size == null || this.size < 1) ? 10 : this.size;
    }
}
