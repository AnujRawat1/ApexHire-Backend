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
public class AnalysisSection {
    private String key;
    private String title;
    private int score;
    private String summary;
    private List<String> points;
}
