package com.ApexHire.resume.model;

import com.ApexHire.resume.enums.RecommendationPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String title;
    private String detail;
    private RecommendationPriority priority;
}
