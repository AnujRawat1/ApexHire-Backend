package com.ApexHire.careermentor.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "career_mentor_sessions")
@CompoundIndex(name = "user_updated_idx", def = "{'userId': 1, 'updatedAt': -1}")
public class CareerMentorSession {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;

    private String targetRole;

    private String careerGoal;

    private String resumeReportId;

    @Builder.Default
    private List<CareerMentorMessage> messages = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareerMentorMessage {
        private String id;
        private String role; // "user" or "assistant"
        private String content;
        private LocalDateTime timestamp;
        @Builder.Default
        private List<String> suggestedFollowUps = new ArrayList<>();
    }
}
