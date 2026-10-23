package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketDetailResponse(
        UUID id,
        String tenantCode,
        String resortName,
        String ownerName,
        String ownerEmail,
        UUID creatorId,
        UUID assigneeId,
        String assigneeName,
        String assigneeEmail,
        String title,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        String description,
        String resolutionNotes,
        List<TicketReplyResponse> replies,
        List<TicketAttachmentResponse> attachments,
        List<TicketHistoryResponse> history,
        Instant createdAt,
        Instant updatedAt
) {
    public record TicketAttachmentResponse(
            UUID id,
            String name,
            String type,
            long size,
            String storageKey,
            String dataUrl,
            Instant timestamp
    ) {}

    public record TicketHistoryResponse(
            UUID id,
            String actorName,
            String actorRole,
            String action,
            String details,
            Instant timestamp
    ) {}
}
