package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
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
        List<TicketDetailResponse.TicketAttachmentResponse> attachments,
        List<TicketDetailResponse.TicketHistoryResponse> history,
        Instant createdAt,
        Instant updatedAt
) {
    public TicketResponse(UUID id, String tenantCode, String resortName, String ownerName, String ownerEmail,
                          String title, TicketCategory category, TicketPriority priority, TicketStatus status,
                          UUID assigneeId, String assigneeName, String description,
                          Instant createdAt, Instant updatedAt) {
        this(id, tenantCode, resortName, ownerName, ownerEmail, null, assigneeId, assigneeName, null,
                title, category, priority, status, description, null, List.of(), List.of(), List.of(),
                createdAt, updatedAt);
    }
}
