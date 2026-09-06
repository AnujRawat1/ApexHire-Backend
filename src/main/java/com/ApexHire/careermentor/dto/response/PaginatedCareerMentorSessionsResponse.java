package com.ApexHire.careermentor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedCareerMentorSessionsResponse {

    private List<CareerMentorSessionResponse> sessions;
    private int currentPage;
    private int totalPages;
    private long totalSessions;
    private boolean hasNext;
    private boolean hasPrevious;
}
