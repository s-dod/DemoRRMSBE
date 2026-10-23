package com.example.rrms.superadmin.DTO.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(
        @Size(max = 100)
        String resortName,

        @Size(max = 255)
        String address,

        @Size(max = 60)
        String city,

        @Size(max = 60)
        String state,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        String phone,

        String tier,
        Integer roomsCount
) {
}
