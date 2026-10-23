package com.example.rrms.superadmin.DTO.request;

import com.example.rrms.superadmin.enums.AuditSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuditEventRequest(
        @NotBlank(message = "Actor is required")
        String actor,

        @NotBlank(message = "Action is required")
        String action,

        String details,

        @NotNull(message = "Severity is required")
        AuditSeverity severity,

        String tenantCode,
        String ipAddress
) {
}
