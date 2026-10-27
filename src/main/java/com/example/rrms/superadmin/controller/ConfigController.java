package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.request.UpdateConfigRequest;
import com.example.rrms.superadmin.DTO.response.PlatformConfigResponse;
import com.example.rrms.superadmin.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public PlatformConfigResponse getConfig() {
        return configService.getConfig();
    }

    @PutMapping
    public PlatformConfigResponse updateConfig(@Valid @RequestBody UpdateConfigRequest request) {
        return configService.updateConfig(request);
    }

    @PostMapping("/maintenance/toggle")
    public PlatformConfigResponse toggleMaintenance() {
        return configService.toggleMaintenance();
    }
}
