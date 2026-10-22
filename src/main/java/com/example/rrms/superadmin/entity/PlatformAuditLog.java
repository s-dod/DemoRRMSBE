package com.example.rrms.superadmin.entity;

import com.example.rrms.superadmin.enums.AuditSeverity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "platform_audit_logs",
    schema = "super_admin",
    indexes = {
        @Index(name = "idx_platform_audit_logs_created_at", columnList = "created_at")
    }
)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditSeverity severity = AuditSeverity.INFO;

    @Column(name = "tenant_code", length = 50)
    private String tenantCode;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("timestamp")
    private Instant createdAt = Instant.now();

    public Instant getTimestamp() {
        return createdAt;
    }

    public PlatformAuditLog(String actor, String action, String details, AuditSeverity severity,
                            String tenantCode, String ipAddress, Instant createdAt) {
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.severity = severity != null ? severity : AuditSeverity.INFO;
        this.tenantCode = tenantCode;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public PlatformAuditLog(String actor, String action, String details, AuditSeverity severity,
                            String tenantCode, String ipAddress) {
        this(actor, action, details, severity, tenantCode, ipAddress, Instant.now());
    }
}
