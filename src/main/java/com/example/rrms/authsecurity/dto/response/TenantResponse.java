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
        String tier,
        String location,
        Integer roomsCount,
        String resortEmail,
        UUID createdBy,
        Instant createdAt,
        UUID ownerId,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        UserStatus ownerStatus
) {
    public TenantResponse(UUID id, String code, String name, TenantStatus status, String tier, String location, Integer roomsCount, UUID createdBy, Instant createdAt, UUID ownerId, String ownerName, String ownerEmail, String ownerPhone, UserStatus ownerStatus) {
        this(id, code, name, status, tier, location, roomsCount, ownerEmail, createdBy, createdAt, ownerId, ownerName, ownerEmail, ownerPhone, ownerStatus);
    }

    public TenantResponse(UUID id, String code, String name, TenantStatus status, String tier, String location, Integer roomsCount, UUID createdBy, Instant createdAt) {
        this(id, code, name, status, tier, location, roomsCount, null, createdBy, createdAt, null, null, null, null, null);
    }

    public TenantResponse(UUID id, String code, String name, TenantStatus status, UUID createdBy, Instant createdAt) {
        this(id, code, name, status, "STARTER", null, 0, null, createdBy, createdAt, null, null, null, null, null);
    }
}

