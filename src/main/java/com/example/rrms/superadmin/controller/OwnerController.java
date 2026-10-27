package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.request.UpdateOwnerRequest;
import com.example.rrms.superadmin.DTO.response.OwnerResponse;
import com.example.rrms.superadmin.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public List<OwnerResponse> getAllOwners() {
        return ownerService.getAll();
    }

    @GetMapping("/{id}")
    public OwnerResponse getOwnerById(@PathVariable UUID id) {
        return ownerService.getById(id);
    }

    @PutMapping("/{id}")
    public OwnerResponse updateOwner(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOwnerRequest request
    ) {
        return ownerService.update(id, request);
    }

    @PostMapping("/{id}/reset-password")
    public Map<String, Object> resetOwnerPassword(@PathVariable UUID id) {
        return ownerService.resetPassword(id);
    }
}
