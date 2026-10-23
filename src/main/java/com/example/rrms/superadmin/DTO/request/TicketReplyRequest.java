package com.example.rrms.superadmin.DTO.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record TicketReplyRequest(
        @NotBlank(message = "Reply message cannot be blank")
        String message,

        UUID senderId,
        String senderName,
        String senderRole
) {
}
