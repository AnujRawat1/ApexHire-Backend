package com.ApexHire.careermentor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerMentorSessionResponse {

    private String id;
    private String userId;
    private String title;
    private String targetRole;
    private String careerGoal;
    private String resumeReportId;
    private List<CareerMentorMessageResponse> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
