package com.ApexHire.coverletter.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cover_letters")
public class CoverLetter {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String companyName;
    private String targetRole;
    private String tone;
    private String jobDescription;
    private String skills;
    private String additionalInfo;

    private String resumeReportId;

    private String content;

    @Builder.Default
    private List<String> keyHighlights = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
