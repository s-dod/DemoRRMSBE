package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String code,
        String name,
        TenantStatus status,
        UUID createdBy,
        Instant createdAt,
        UUID ownerId,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        UserStatus ownerStatus
) {
    public TenantResponse(UUID id, String code, String name, TenantStatus status, UUID createdBy, Instant createdAt) {
        this(id, code, name, status, createdBy, createdAt, null, null, null, null, null);
    }
}

