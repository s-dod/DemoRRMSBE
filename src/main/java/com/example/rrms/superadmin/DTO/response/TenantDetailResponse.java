package com.example.rrms.superadmin.DTO.response;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public record TenantDetailResponse(
        UUID id,
        String resortName,
        String slug,
        String address,
        String city,
        String state,
        String phone,
        TenantStatus status,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        Instant createdAt
) {
}
