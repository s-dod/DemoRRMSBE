package com.example.rrms.authsecurity.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TenantStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    PENDING("Pending"),
    ARCHIVED("Archived");

    private final String displayName;

    TenantStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TenantStatus fromValue(String value) {
        if (value == null) return null;
        for (TenantStatus status : TenantStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return TenantStatus.valueOf(value.toUpperCase());
    }
}
