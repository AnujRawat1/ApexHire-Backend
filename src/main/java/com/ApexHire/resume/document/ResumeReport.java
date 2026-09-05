package com.ApexHire.resume.document;

import com.ApexHire.resume.model.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resume_reports")
public class ResumeReport {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String fileName;
    private Long fileSize;
    private String fileStorageKey;

    private String targetRole;
    private String experienceLevel;
    private String jobDescription;
    private String resumeText;

    private AnalysisResult analysis;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();
}
