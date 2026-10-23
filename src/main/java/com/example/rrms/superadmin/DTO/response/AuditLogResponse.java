package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.superadmin.enums.AuditSeverity;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String actor,
        String action,
        String details,
        AuditSeverity severity,
        String tenantCode,
        String ipAddress,
        Instant timestamp
) {
}
