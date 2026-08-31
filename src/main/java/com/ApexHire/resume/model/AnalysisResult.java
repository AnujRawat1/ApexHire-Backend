package com.ApexHire.resume.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private int overallScore;
    private int atsScore;
    private Integer jobMatchScore;
    private String summary;
    private List<AnalysisSection> sections;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> missingSkills;
    private List<String> missingKeywords;
    private List<Recommendation> recommendations;
    private List<String> improvements;
}
