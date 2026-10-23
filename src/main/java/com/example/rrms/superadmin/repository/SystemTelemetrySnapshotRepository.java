package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.SystemTelemetrySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.Optional;

public interface SystemTelemetrySnapshotRepository extends JpaRepository<SystemTelemetrySnapshot, UUID> {
    Optional<SystemTelemetrySnapshot> findFirstByOrderByCreatedAtDesc();
}
