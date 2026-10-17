package com.example.rrms.superadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "system_telemetry_snapshots",
    schema = "super_admin",
    indexes = {
        @Index(name = "idx_system_telemetry_snapshots_created_at", columnList = "created_at")
    }
)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemTelemetrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "active_tenants", nullable = false)
    private int activeTenants;

    @Column(name = "total_rooms_managed", nullable = false)
    private int totalRoomsManaged;

    @Column(name = "cpu_usage_percent", nullable = false)
    private double cpuUsagePercent;

    @Column(name = "memory_usage_percent", nullable = false)
    private double memoryUsagePercent;

    @Column(name = "open_critical_tickets", nullable = false)
    private int openCriticalTickets;

    @Column(name = "api_requests_per_minute", nullable = false)
    private int apiRequestsPerMinute;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SystemTelemetrySnapshot(int activeTenants, int totalRoomsManaged, double cpuUsagePercent,
                                   double memoryUsagePercent, int openCriticalTickets,
                                   int apiRequestsPerMinute, Instant createdAt) {
        this.activeTenants = activeTenants;
        this.totalRoomsManaged = totalRoomsManaged;
        this.cpuUsagePercent = cpuUsagePercent;
        this.memoryUsagePercent = memoryUsagePercent;
        this.openCriticalTickets = openCriticalTickets;
        this.apiRequestsPerMinute = apiRequestsPerMinute;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public SystemTelemetrySnapshot(int activeTenants, int totalRoomsManaged, double cpuUsagePercent,
                                   double memoryUsagePercent, int openCriticalTickets,
                                   int apiRequestsPerMinute) {
        this(activeTenants, totalRoomsManaged, cpuUsagePercent, memoryUsagePercent, openCriticalTickets, apiRequestsPerMinute, Instant.now());
    }

    public static SystemTelemetrySnapshot create(int activeTenants, int totalRoomsManaged, double cpuUsagePercent,
                                                 double memoryUsagePercent, int openCriticalTickets,
                                                 int apiRequestsPerMinute) {
        return new SystemTelemetrySnapshot(activeTenants, totalRoomsManaged, cpuUsagePercent, memoryUsagePercent, openCriticalTickets, apiRequestsPerMinute, Instant.now());
    }
}
