package com.ApexHire.resume.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExperienceLevel {
    JUNIOR("Junior (0-2 years)"),
    MID_LEVEL("Mid-level (2-5 years)"),
    SENIOR("Senior (5-10 years)"),
    STAFF("Staff+ (10+ years)");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static ExperienceLevel fromDisplayName(String displayName) {
        for (ExperienceLevel level : values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown experience level: " + displayName);
    }
}
