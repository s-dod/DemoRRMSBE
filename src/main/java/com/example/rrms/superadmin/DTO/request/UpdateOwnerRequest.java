package com.example.rrms.superadmin.DTO.request;

import com.example.rrms.authsecurity.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOwnerRequest(
        @Size(max = 100)
        String name,

        @Email(message = "Invalid email format")
        String email,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        String phone,

        UserStatus status
) {
}
