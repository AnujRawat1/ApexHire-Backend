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
public class CareerMentorMessageResponse {

    private String id;
    private String role;
    private String content;
    private LocalDateTime timestamp;
    private List<String> suggestedFollowUps;
}
