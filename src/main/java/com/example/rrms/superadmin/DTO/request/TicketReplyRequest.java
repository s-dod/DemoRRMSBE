package com.example.rrms.superadmin.DTO.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record TicketReplyRequest(
        @NotBlank(message = "Reply message cannot be blank")
        String message,

        UUID senderId,
        String senderName,
        String senderRole,
        List<TicketAttachmentInput> attachments,
        List<TicketAttachmentInput> replyAttachments
) {
    public TicketReplyRequest(String message, UUID senderId, String senderName, String senderRole) {
        this(message, senderId, senderName, senderRole, List.of(), List.of());
    }
}
