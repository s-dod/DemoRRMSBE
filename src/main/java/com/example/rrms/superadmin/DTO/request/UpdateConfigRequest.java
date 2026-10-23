package com.example.rrms.superadmin.DTO.request;

public record UpdateConfigRequest(
        Boolean maintenanceMode,
        Boolean registrationOpen,
        Integer maxTenants,
        Integer sessionTimeoutMinutes,
        Integer telemetryIntervalSeconds,
        String backupSchedule,
        Boolean enforceMfa,
        Integer logRetentionDays,
        String defaultTimezone
) {
}
