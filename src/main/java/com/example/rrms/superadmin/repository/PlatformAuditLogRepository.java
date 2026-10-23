package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.PlatformAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlatformAuditLogRepository extends JpaRepository<PlatformAuditLog, UUID> {
}