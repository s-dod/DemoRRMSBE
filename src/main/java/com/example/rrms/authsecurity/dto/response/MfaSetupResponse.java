package com.example.rrms.authsecurity.dto.response;

public record MfaSetupResponse(
        String secret,
        String otpAuthUri
) {}
