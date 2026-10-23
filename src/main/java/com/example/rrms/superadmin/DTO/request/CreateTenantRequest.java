package com.example.rrms.superadmin.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank(message = "Resort name is required")
        @Size(max = 100, message = "Resort name cannot exceed 100 characters")
        String resortName,

        @Size(max = 255)
        String address,

        @Size(max = 60)
        String city,

        @Size(max = 60)
        String state,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        String resortPhone,

        @NotBlank(message = "Owner name is required")
        @Size(max = 100)
        String ownerName,

        @NotBlank(message = "Owner email is required")
        @Email(message = "Invalid email format")
        String ownerEmail,

        @NotBlank(message = "Owner phone is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        String ownerPhone,

        @NotBlank(message = "Owner password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        String ownerPassword,

        String tenantCode,
        String tier,
        Integer roomsCount
) {
}
