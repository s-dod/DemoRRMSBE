package com.example.rrms.authsecurity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank String tenantCode,
        @NotBlank String resortName,
        @NotBlank String ownerName,
        @NotBlank @Email String ownerEmail,
        String tier,
        String location,
        Integer roomsCount
) {
    public CreateTenantRequest(String tenantCode, String resortName, String ownerName, String ownerEmail) {
        this(tenantCode, resortName, ownerName, ownerEmail, "STARTER", null, 0);
    }
}
