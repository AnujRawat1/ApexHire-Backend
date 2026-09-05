package com.ApexHire.resume.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecommendationPriority {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    RecommendationPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RecommendationPriority fromValue(String value) {
        for (RecommendationPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown recommendation priority: " + value);
    }
}
