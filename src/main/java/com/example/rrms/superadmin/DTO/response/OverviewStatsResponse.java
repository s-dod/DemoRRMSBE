package com.example.rrms.superadmin.DTO.response;

public record OverviewStatsResponse(
        long totalTenants,
        long activeTenants,
        long suspendedTenants,
        long totalOwners,
        long openTickets,
        long criticalTickets,
        double cpuUsagePercent,
        double memoryUsagePercent,
        boolean maintenanceMode
) {
}
