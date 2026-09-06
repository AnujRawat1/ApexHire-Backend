package com.ApexHire.careermentor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    private String message;

    private String targetRole;

    private String careerGoal;

    private String resumeReportId;
}
