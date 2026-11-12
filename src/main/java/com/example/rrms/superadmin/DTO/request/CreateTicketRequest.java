package com.example.rrms.superadmin.DTO.request;

import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank(message = "Tenant code is required")
        String tenantCode,

        String resortName,
        String ownerName,
        String ownerEmail,
        UUID creatorId,

        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
        String title,

        @NotNull(message = "Category is required")
        TicketCategory category,

        @NotNull(message = "Priority is required")
        TicketPriority priority,

        @NotBlank(message = "Description is required")
        String description,

        List<TicketAttachmentInput> attachments
) {
    public CreateTicketRequest(String tenantCode, String resortName, String ownerName, String ownerEmail,
                               UUID creatorId, String title, TicketCategory category,
                               TicketPriority priority, String description) {
        this(tenantCode, resortName, ownerName, ownerEmail, creatorId, title, category, priority, description, List.of());
    }
}
