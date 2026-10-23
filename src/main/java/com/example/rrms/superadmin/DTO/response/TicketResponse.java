package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String tenantCode,
        String resortName,
        String ownerName,
        String ownerEmail,
        String title,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        UUID assigneeId,
        String assigneeName,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
