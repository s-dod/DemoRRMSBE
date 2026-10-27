package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.UpdateTicketStatusRequest;
import com.example.rrms.authsecurity.dto.response.SystemMonitoringResponse;
import com.example.rrms.authsecurity.dto.response.TechnicalTicketResponse;
import com.example.rrms.authsecurity.service.PlatformSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformSupportService platformSupportService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/tickets")
    public List<TechnicalTicketResponse> getAllTickets() {
        return platformSupportService.getAllPlatformTickets();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/tickets/{id}/status")
    public TechnicalTicketResponse updateTicketStatus(@PathVariable UUID id, @Valid @RequestBody UpdateTicketStatusRequest updateTicketStatusRequest) {
        return platformSupportService.updateTicketStatus(id, updateTicketStatusRequest);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/system-monitoring")
    public SystemMonitoringResponse getSystemMonitoring() {
        return platformSupportService.getSystemMonitoring();
    }
}
