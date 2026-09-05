package com.ApexHire.resume.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeResumeRequest {
    @NotBlank(message = "Resume text is required")
    @Size(min = 30, message = "Resume text is too short to analyze")
    private String resumeText;

    @NotBlank(message = "Target role is required")
    @Size(min = 2, max = 80, message = "Target role must be between 2 and 80 characters")
    private String targetRole;

    @NotBlank(message = "Experience level is required")
    @Size(min = 2, max = 60, message = "Experience level must be between 2 and 60 characters")
    private String experienceLevel;

    @Size(max = 120, message = "Resume title must not exceed 120 characters")
    private String resumeTitle;

    @Size(max = 20000, message = "Job description must not exceed 20000 characters")
    private String jobDescription;
}
