package com.example.rrms.authsecurity.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TechnicalTicketResponse(
        UUID id,
        UUID tenantId,
        String tenantCode,
        String resortName,
        UUID ownerId,
        String ownerName,
        String ownerEmail,
        String title,
        String category,
        String priority,
        String status,
        String description,
        String resolutionNotes,
        Instant createdAt,
        Instant updatedAt
) {}
