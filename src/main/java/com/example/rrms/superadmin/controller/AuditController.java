package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.request.AuditEventRequest;
import com.example.rrms.superadmin.DTO.response.AuditLogResponse;
import com.example.rrms.superadmin.enums.AuditSeverity;
import com.example.rrms.superadmin.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/audit", "/api/audit"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public List<AuditLogResponse> searchAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String tenantCode,
            @RequestParam(required = false) AuditSeverity severity
    ) {
        return auditService.search(action, actor, tenantCode, severity);
    }

    @PostMapping
    public ResponseEntity<AuditLogResponse> recordEvent(@Valid @RequestBody AuditEventRequest request) {
        AuditLogResponse recorded = auditService.record(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recorded);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = auditService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
