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
        Instant createdAt
) {
}
