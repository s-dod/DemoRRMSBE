package com.example.rrms.superadmin.DTO.request;

public record TicketAttachmentInput(
        String name,
        String type,
        Long size,
        String dataUrl,
        String storageKey
) {
}

