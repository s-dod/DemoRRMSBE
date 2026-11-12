package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantDetailResponse(
        UUID id,
        String code,
        String name,
        TenantStatus status,
        String tier,
        String location,
        Integer roomsCount,
        String resortEmail,
        String address,
        String city,
        String state,
        String phone,
        UUID createdBy,
        UUID ownerId,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        Instant createdAt
) {
    public String getResortName() {
        return name;
    }

    public String getSlug() {
        return code;
    }
}
