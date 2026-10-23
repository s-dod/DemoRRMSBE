package com.example.rrms.superadmin.service;

import com.example.rrms.superadmin.DTO.request.AuditEventRequest;
import com.example.rrms.superadmin.DTO.response.AuditLogResponse;
import com.example.rrms.superadmin.entity.PlatformAuditLog;
import com.example.rrms.superadmin.enums.AuditSeverity;
import com.example.rrms.superadmin.repository.PlatformAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service("superAdminAuditService")
@RequiredArgsConstructor
public class AuditService {

    private final PlatformAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> search(String action, String actor, String tenantCode, AuditSeverity severity) {
        List<PlatformAuditLog> logs;
        if (tenantCode != null && !tenantCode.isBlank()) {
            logs = auditLogRepository.findByTenantCodeOrderByCreatedAtDesc(tenantCode.trim());
        } else if (severity != null) {
            logs = auditLogRepository.findBySeverityOrderByCreatedAtDesc(severity);
        } else {
            logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        }

        return logs.stream()
                .filter(log -> action == null || action.isBlank() || log.getAction().toLowerCase().contains(action.trim().toLowerCase()))
                .filter(log -> actor == null || actor.isBlank() || log.getActor().toLowerCase().contains(actor.trim().toLowerCase()))
                .filter(log -> severity == null || log.getSeverity() == severity)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuditLogResponse record(AuditEventRequest req) {
        PlatformAuditLog log = new PlatformAuditLog(
                req.actor(),
                req.action(),
                req.details(),
                req.severity(),
                req.tenantCode(),
                req.ipAddress()
        );
        return toResponse(auditLogRepository.save(log));
    }

    @Transactional
    public void record(String actor, String action, String details, AuditSeverity severity, String tenantCode, String ipAddress) {
        PlatformAuditLog log = new PlatformAuditLog(
                actor != null ? actor : "SUPER_ADMIN",
                action,
                details,
                severity != null ? severity : AuditSeverity.INFO,
                tenantCode,
                ipAddress
        );
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<PlatformAuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("Log ID,Actor,Action,Severity,Tenant Code,IP Address,Timestamp,Details\n");

        for (PlatformAuditLog log : logs) {
            csv.append(escapeCsv(log.getId() != null ? log.getId().toString() : "")).append(",")
               .append(escapeCsv(log.getActor())).append(",")
               .append(escapeCsv(log.getAction())).append(",")
               .append(escapeCsv(log.getSeverity() != null ? log.getSeverity().name() : "")).append(",")
               .append(escapeCsv(log.getTenantCode() != null ? log.getTenantCode() : "")).append(",")
               .append(escapeCsv(log.getIpAddress() != null ? log.getIpAddress() : "")).append(",")
               .append(escapeCsv(log.getTimestamp() != null ? log.getTimestamp().toString() : "")).append(",")
               .append(escapeCsv(log.getDetails() != null ? log.getDetails() : "")).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public AuditLogResponse toResponse(PlatformAuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActor(),
                log.getAction(),
                log.getDetails(),
                log.getSeverity(),
                log.getTenantCode(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
