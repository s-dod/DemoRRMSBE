package com.example.rrms.authsecurity.dto.request;

import com.example.rrms.authsecurity.domain.enums.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank String title,
        @NotNull StaffType department,
        UUID assignedStaffId
) {}
