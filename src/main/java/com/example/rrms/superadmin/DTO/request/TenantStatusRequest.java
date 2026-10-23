package com.example.rrms.superadmin.DTO.request;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TenantStatusRequest(
        @NotNull(message = "Status is required")
        TenantStatus status,

        @Size(max = 255)
        String reason
) {
}
