package com.example.rrms.superadmin.DTO.response;

import java.time.Instant;

public record TelemetryResponse(
        int activeTenants,
        int totalRoomsManaged,
        double cpuUsagePercent,
        double memoryUsagePercent,
        int openCriticalTickets,
        int apiRequestsPerMinute,
        long uptimeSeconds,
        int activeThreads,
        int totalTenants,
        long totalUsers,
        Instant timestamp
) {
}
