package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.modal.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
