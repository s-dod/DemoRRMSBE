package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.request.CreateTenantRequest;
import com.example.rrms.superadmin.DTO.request.TenantStatusRequest;
import com.example.rrms.superadmin.DTO.request.UpdateTenantRequest;
import com.example.rrms.superadmin.DTO.response.TenantDetailResponse;
import com.example.rrms.superadmin.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/tenants", "/api/tenants"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public List<TenantDetailResponse> getAllTenants() {
        return tenantService.getAll();
    }

    @GetMapping("/page")
    public Page<TenantDetailResponse> getTenantsPaged(Pageable pageable) {
        return tenantService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public TenantDetailResponse getTenantById(@PathVariable UUID id) {
        return tenantService.getById(id);
    }

    @PostMapping
    public ResponseEntity<TenantDetailResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantDetailResponse created = tenantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TenantDetailResponse updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        return tenantService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TenantDetailResponse updateTenantStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TenantStatusRequest request
    ) {
        return tenantService.updateStatus(id, request);
    }
}
