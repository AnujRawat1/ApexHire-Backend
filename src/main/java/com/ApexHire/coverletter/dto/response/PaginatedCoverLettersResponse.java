package com.ApexHire.coverletter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedCoverLettersResponse {
    private List<CoverLetterResponse> items;
    private long total;
    private int page;
    private int limit;
    private int totalPages;
}
