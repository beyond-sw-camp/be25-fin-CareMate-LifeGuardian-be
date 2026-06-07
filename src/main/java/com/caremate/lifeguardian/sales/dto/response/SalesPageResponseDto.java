package com.caremate.lifeguardian.sales.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SalesPageResponseDto {

    private int page;

    private int size;

    private long totalCount;

    private int totalPages;

    private List<SalesListResponseDto> items;
}
