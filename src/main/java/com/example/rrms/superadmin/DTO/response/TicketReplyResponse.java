package com.example.rrms.superadmin.DTO.response;

import java.time.Instant;
import java.util.UUID;

public record TicketReplyResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String senderRole,
        String message,
        Instant createdAt
) {
}
