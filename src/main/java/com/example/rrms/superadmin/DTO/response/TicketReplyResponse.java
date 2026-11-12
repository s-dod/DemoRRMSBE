package com.example.rrms.superadmin.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketReplyResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String senderRole,
        String message,
        List<TicketDetailResponse.TicketAttachmentResponse> attachments,
        @JsonProperty("timestamp") Instant timestamp
) {
    public TicketReplyResponse(UUID id, UUID senderId, String senderName, String senderRole, String message, Instant timestamp) {
        this(id, senderId, senderName, senderRole, message, List.of(), timestamp);
    }

    public Instant getCreatedAt() {
        return timestamp;
    }
}
