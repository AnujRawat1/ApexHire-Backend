package com.ApexHire.resume.dto.response;

import com.ApexHire.resume.model.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeReportResponse {
    private String id;
    private String title;
    private String fileName;
    private Long fileSize;
    private String targetRole;
    private String experienceLevel;
    private String jobDescription;
    private AnalysisResult analysis;
    private LocalDateTime createdAt;
    private LocalDateTime analyzedAt;
}
