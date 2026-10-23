package com.example.rrms.superadmin.DTO.request;

import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;

import java.util.UUID;

public record UpdateTicketRequest(
        TicketStatus status,
        TicketPriority priority,
        UUID assigneeId,
        String assigneeName,
        String assigneeEmail,
        String resolutionNotes
) {
}
