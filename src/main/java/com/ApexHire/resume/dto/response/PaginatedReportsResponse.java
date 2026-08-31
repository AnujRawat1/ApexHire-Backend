package com.ApexHire.resume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedReportsResponse {
    private List<ResumeReportResponse> reports;
    private long total;
    private int page;
    private int limit;
    private int totalPages;
}
