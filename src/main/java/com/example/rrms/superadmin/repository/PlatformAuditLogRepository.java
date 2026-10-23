package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.PlatformAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import com.example.rrms.superadmin.enums.AuditSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PlatformAuditLogRepository extends JpaRepository<PlatformAuditLog, UUID> {
    List<PlatformAuditLog> findAllByOrderByCreatedAtDesc();
    Page<PlatformAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<PlatformAuditLog> findTop10ByOrderByCreatedAtDesc();
    List<PlatformAuditLog> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
    List<PlatformAuditLog> findBySeverityOrderByCreatedAtDesc(AuditSeverity severity);
}