package com.example.rrms.superadmin.DTO.response;

import java.util.List;

public record OverviewActivityResponse(
        List<TicketResponse> recentTickets,
        List<AuditLogResponse> recentAuditLogs,
        List<String> activeAlerts
) {
}
