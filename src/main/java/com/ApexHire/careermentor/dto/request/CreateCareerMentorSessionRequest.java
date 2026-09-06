package com.ApexHire.careermentor.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCareerMentorSessionRequest {

    @Size(max = 120, message = "Session title cannot exceed 120 characters")
    private String title;

    private String targetRole;

    private String careerGoal;

    private String resumeReportId;

    private String initialMessage;
}
