package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.*;
import com.example.rrms.authsecurity.dto.response.AuthResponse;
import com.example.rrms.authsecurity.dto.response.RefreshRequest;
import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/superadmin/login")
    public AuthResponse superAdminLogin(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.superAdminLogin(loginRequest);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @GetMapping("/tenants")
    public List<TenantResponse> getTenants() {
        return authService.getPublicTenants();
    }

    @PostMapping("/mfa/verify")
    public AuthResponse mfa(@Valid @RequestBody MfaVerifyRequest mfaVerifyRequest) {
        return authService.verifyMfa(mfaVerifyRequest);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        return authService.refresh(refreshRequest);
    }

    @PostMapping("/logout")
    public void logout() {
        authService.logout(CurrentUser.get().getId());
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(CurrentUser.get().getId(), changePasswordRequest);
    }

    @PostMapping("/reset-temp-password")
    public java.util.Map<String, String> resetTempPassword(@Valid @RequestBody ResetTempPasswordRequest resetTempPasswordRequest) {
        authService.resetTemporaryPassword(resetTempPasswordRequest);
        return java.util.Map.of("message", "Temporary password changed successfully. You may now log in.");
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getCurrentUser();
    }
}
