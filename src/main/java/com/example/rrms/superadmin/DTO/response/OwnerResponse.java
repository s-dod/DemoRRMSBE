package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.authsecurity.domain.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record OwnerResponse(
        UUID id,
        UUID tenantId,
        String resortName,
        String tenantCode,
        String name,
        String email,
        String phone,
        UserStatus status,
        boolean active,
        String module,
        String role,
        UUID createdBy,
        String shift,
        Instant createdAt
) {
    public OwnerResponse(UUID id, UUID tenantId, String resortName, String tenantCode,
                         String name, String email, String phone, UserStatus status, Instant createdAt) {
        this(id, tenantId, resortName, tenantCode, name, email, phone, status,
                status == UserStatus.ACTIVE, "Owner", "Owner", null, "", createdAt);
    }
}
