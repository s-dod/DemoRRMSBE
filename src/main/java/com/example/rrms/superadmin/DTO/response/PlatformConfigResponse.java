package com.example.rrms.superadmin.DTO.response;

import java.time.Instant;

public record PlatformConfigResponse(
        String id,
        boolean maintenanceMode,
        boolean registrationOpen,
        int maxTenants,
        int sessionTimeoutMinutes,
        int telemetryIntervalSeconds,
        String backupSchedule,
        boolean enforceMfa,
        int logRetentionDays,
        String defaultTimezone,
        Instant updatedAt
) {
}
