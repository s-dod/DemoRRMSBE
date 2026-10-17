package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.CreateTenantRequest;
import com.example.rrms.authsecurity.dto.request.CreateUserRequest;
import com.example.rrms.authsecurity.dto.response.CreatedUserResponse;
import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.service.UserProvisioningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProvisioningController {

    private final UserProvisioningService userProvisioningService;

    @PreAuthorize("hasAuthority('TENANT_CREATE') and hasAuthority('OWNER_CREATE')")
    @PostMapping("/api/platform/tenants")
    public CreatedUserResponse createTenant(@Valid @RequestBody CreateTenantRequest createTenantRequest) {
        return userProvisioningService.createTenantWithOwner(createTenantRequest);
    }

    @PreAuthorize("hasAuthority('TENANT_VIEW')")
    @GetMapping("/api/platform/tenants")
    public List<TenantResponse> getTenants() {
        return userProvisioningService.getTenantRepository();
    }

    @PreAuthorize("hasAuthority('MANAGEMENT_CREATE')")
    @PostMapping("/api/owner/management")
    public CreatedUserResponse createManagement(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return userProvisioningService.createUser(Role.MANAGEMENT, createUserRequest);
    }

    @PreAuthorize("hasAuthority('MANAGEMENT_VIEW')")
    @GetMapping("/api/owner/management")
    public List<UserResponse> getManagement() {
        return userProvisioningService.getUsersByRole(Role.MANAGEMENT);
    }

    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    @PostMapping("/api/management/staff")
    public CreatedUserResponse createStaff(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return userProvisioningService.createUser(Role.STAFF, createUserRequest);
    }

    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    @GetMapping("/api/management/staff")
    public List<UserResponse> getStaff() {
        return userProvisioningService.getUsersByRole(Role.STAFF);
    }

    /** Shared route: MANAGEMENT and RECEPTIONIST both hold GUEST_CREATE; nobody else does. */
    @PreAuthorize("hasAuthority('GUEST_CREATE')")
    @PostMapping("/api/guests")
    public CreatedUserResponse createGuest(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return userProvisioningService.createUser(Role.GUEST, createUserRequest);
    }

    @PreAuthorize("hasAuthority('GUEST_VIEW')")
    @GetMapping("/api/guests")
    public List<UserResponse> getGuests() {
        return userProvisioningService.getUsersByRole(Role.GUEST);
    }
}
