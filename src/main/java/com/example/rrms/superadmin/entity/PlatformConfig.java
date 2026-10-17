package com.example.rrms.superadmin.entity;

import com.example.rrms.common.exception.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_configs", schema = "super_admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformConfig extends AuditableEntity {

    public static final String GLOBAL_CONFIG = "GLOBAL_CONFIG";

    @Id
    @Column(length = 50)
    private String id = GLOBAL_CONFIG;

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode = false;

    @Column(name = "registration_open", nullable = false)
    private boolean registrationOpen = true;

    @Column(name = "max_tenants", nullable = false)
    private int maxTenants = 500;

    @Column(name = "session_timeout_minutes", nullable = false)
    private int sessionTimeoutMinutes = 60;

    @Column(name = "telemetry_interval_seconds", nullable = false)
    private int telemetryIntervalSeconds = 30;

    @Column(name = "backup_schedule", nullable = false, length = 50)
    private String backupSchedule = "0 2 * * *";

    @Column(name = "enforce_mfa", nullable = false)
    private boolean enforceMfa = false;

    @Column(name = "log_retention_days", nullable = false)
    private int logRetentionDays = 90;

    @Column(name = "default_timezone", nullable = false, length = 50)
    private String defaultTimezone = "UTC";

    public static PlatformConfig createDefault() {
        PlatformConfig config = new PlatformConfig();
        config.id = GLOBAL_CONFIG;
        config.maintenanceMode = false;
        config.registrationOpen = true;
        config.maxTenants = 500;
        config.sessionTimeoutMinutes = 60;
        config.telemetryIntervalSeconds = 30;
        config.backupSchedule = "0 2 * * *";
        config.enforceMfa = false;
        config.logRetentionDays = 90;
        config.defaultTimezone = "UTC";
        return config;
    }

    public boolean setMaintenanceMode(boolean enabled) {
        if (this.maintenanceMode == enabled) {
            return false;
        }
        this.maintenanceMode = enabled;
        return true;
    }

    public void updateSecuritySettings(boolean enforceMfa, int sessionTimeoutMinutes) {
        if (sessionTimeoutMinutes < 5) {
            throw ApiException.badRequest("INVALID_TIMEOUT", "Session timeout must be at least 5 minutes");
        }
        this.enforceMfa = enforceMfa;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }
}
