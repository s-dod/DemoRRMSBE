package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CreatedUserResponse(
        UUID id,
        String email,
        Role role,
        String temporaryPassword
) {}
