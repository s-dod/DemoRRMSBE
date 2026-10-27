package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.response.TelemetryResponse;
import com.example.rrms.superadmin.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/telemetry")
    public TelemetryResponse getTelemetry() {
        return monitoringService.getTelemetry();
    }

    @PostMapping("/sync")
    public Map<String, Object> syncCluster() {
        return monitoringService.syncCluster();
    }
}
