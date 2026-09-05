package com.ApexHire.resume.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TargetRole {
    FRONTEND_ENGINEER("Frontend Engineer"),
    BACKEND_ENGINEER("Backend Engineer"),
    FULLSTACK_ENGINEER("Fullstack Engineer"),
    MOBILE_ENGINEER("Mobile Engineer"),
    ML_AI_ENGINEER("ML / AI Engineer"),
    INFRASTRUCTURE_DEVOPS_ENGINEER("Infrastructure / DevOps Engineer"),
    DATA_ENGINEER("Data Engineer"),
    EMBEDDED_SYSTEMS_ENGINEER("Embedded / Systems Engineer");

    private final String displayName;

    TargetRole(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static TargetRole fromDisplayName(String displayName) {
        for (TargetRole role : values()) {
            if (role.displayName.equals(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown target role: " + displayName);
    }
}
