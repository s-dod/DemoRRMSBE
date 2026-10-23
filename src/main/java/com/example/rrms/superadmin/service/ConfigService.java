package com.example.rrms.superadmin.service;

import com.example.rrms.superadmin.DTO.request.UpdateConfigRequest;
import com.example.rrms.superadmin.DTO.response.PlatformConfigResponse;
import com.example.rrms.superadmin.entity.PlatformConfig;
import com.example.rrms.superadmin.enums.AuditSeverity;
import com.example.rrms.superadmin.repository.PlatformConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final PlatformConfigRepository configRepository;
    private final AuditService auditService;

    @Transactional
    public PlatformConfigResponse getConfig() {
        return toResponse(getOrCreateConfig());
    }

    @Transactional
    public PlatformConfigResponse updateConfig(UpdateConfigRequest req) {
        PlatformConfig config = getOrCreateConfig();

        if (req.maintenanceMode() != null) {
            config.setMaintenanceMode(req.maintenanceMode());
        }

        if (req.enforceMfa() != null || req.sessionTimeoutMinutes() != null) {
            boolean enforceMfa = req.enforceMfa() != null ? req.enforceMfa() : config.isEnforceMfa();
            int timeout = req.sessionTimeoutMinutes() != null ? req.sessionTimeoutMinutes() : config.getSessionTimeoutMinutes();
            config.updateSecuritySettings(enforceMfa, timeout);
        }

        config = configRepository.save(config);

        auditService.record(
                "SUPER_ADMIN",
                "PLATFORM_CONFIG_UPDATE",
                "Updated global platform configurations",
                AuditSeverity.WARNING,
                null,
                null
        );

        return toResponse(config);
    }

    @Transactional
    public PlatformConfigResponse toggleMaintenance() {
        PlatformConfig config = getOrCreateConfig();
        boolean newState = !config.isMaintenanceMode();
        config.setMaintenanceMode(newState);
        config = configRepository.save(config);

        auditService.record(
                "SUPER_ADMIN",
                "MAINTENANCE_MODE_TOGGLE",
                "Toggled platform maintenance mode to: " + (newState ? "ENABLED" : "DISABLED"),
                AuditSeverity.CRITICAL,
                null,
                null
        );

        return toResponse(config);
    }

    private PlatformConfig getOrCreateConfig() {
        return configRepository.findById(PlatformConfig.GLOBAL_CONFIG)
                .orElseGet(() -> configRepository.save(PlatformConfig.createDefault()));
    }

    public PlatformConfigResponse toResponse(PlatformConfig c) {
        return new PlatformConfigResponse(
                c.getId(),
                c.isMaintenanceMode(),
                c.isRegistrationOpen(),
                c.getMaxTenants(),
                c.getSessionTimeoutMinutes(),
                c.getTelemetryIntervalSeconds(),
                c.getBackupSchedule(),
                c.isEnforceMfa(),
                c.getLogRetentionDays(),
                c.getDefaultTimezone(),
                c.getUpdatedAt()
        );
    }
}
