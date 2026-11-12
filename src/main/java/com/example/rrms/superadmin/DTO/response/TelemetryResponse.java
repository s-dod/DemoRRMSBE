package com.example.rrms.superadmin.DTO.response;

import java.time.Instant;
import java.util.UUID;

public record TelemetryResponse(
        UUID id,
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
    public TelemetryResponse(int activeTenants, int totalRoomsManaged, double cpuUsagePercent,
                             double memoryUsagePercent, int openCriticalTickets, int apiRequestsPerMinute,
                             long uptimeSeconds, int activeThreads, int totalTenants, long totalUsers,
                             Instant timestamp) {
        this(null, activeTenants, totalRoomsManaged, cpuUsagePercent, memoryUsagePercent,
                openCriticalTickets, apiRequestsPerMinute, uptimeSeconds, activeThreads,
                totalTenants, totalUsers, timestamp);
    }
}
