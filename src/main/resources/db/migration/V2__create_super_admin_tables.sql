-- Flyway Migration: Super Admin Schema Tables and Defaults
-- Version: V2

CREATE SCHEMA IF NOT EXISTS super_admin;

-- 1. Super Admin Users Table
CREATE TABLE IF NOT EXISTS super_admin.super_admin_users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    mfa_required BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(500),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_super_admin_user_email UNIQUE (email)
);

-- 2. Platform Tickets Table (Aggregate Root)
CREATE TABLE IF NOT EXISTS super_admin.platform_tickets (
    id UUID PRIMARY KEY,
    tenant_code VARCHAR(50) NOT NULL,
    resort_name VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    creator_id UUID NOT NULL,
    assignee_id UUID,
    assignee_name VARCHAR(255),
    assignee_email VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    description TEXT NOT NULL,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Ticket Replies Table (Child of Platform Ticket)
CREATE TABLE IF NOT EXISTS super_admin.ticket_replies (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    sender_role VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_reply_ticket FOREIGN KEY (ticket_id)
        REFERENCES super_admin.platform_tickets(id) ON DELETE CASCADE
);

-- 4. Ticket Attachments Table (Child of Platform Ticket)
CREATE TABLE IF NOT EXISTS super_admin.ticket_attachments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    reply_id UUID,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    data_url TEXT,
    storage_key VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_attachment_ticket FOREIGN KEY (ticket_id)
        REFERENCES super_admin.platform_tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_attachment_reply FOREIGN KEY (reply_id)
        REFERENCES super_admin.ticket_replies(id) ON DELETE CASCADE
);

-- 5. Ticket History Entries Table (Child of Platform Ticket)
CREATE TABLE IF NOT EXISTS super_admin.ticket_history_entries (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    actor_name VARCHAR(255) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id)
        REFERENCES super_admin.platform_tickets(id) ON DELETE CASCADE
);

-- 6. Platform Config Table
CREATE TABLE IF NOT EXISTS super_admin.platform_configs (
    id VARCHAR(50) PRIMARY KEY,
    maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE,
    registration_open BOOLEAN NOT NULL DEFAULT TRUE,
    max_tenants INT NOT NULL DEFAULT 500,
    session_timeout_minutes INT NOT NULL DEFAULT 60,
    telemetry_interval_seconds INT NOT NULL DEFAULT 30,
    backup_schedule VARCHAR(50) NOT NULL DEFAULT '0 2 * * *',
    enforce_mfa BOOLEAN NOT NULL DEFAULT FALSE,
    log_retention_days INT NOT NULL DEFAULT 90,
    default_timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Platform Audit Logs Table (Immutable)
CREATE TABLE IF NOT EXISTS super_admin.platform_audit_logs (
    id UUID PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    severity VARCHAR(50) NOT NULL,
    tenant_code VARCHAR(50),
    ip_address VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. System Telemetry Snapshots Table (Immutable)
CREATE TABLE IF NOT EXISTS super_admin.system_telemetry_snapshots (
    id UUID PRIMARY KEY,
    active_tenants INT NOT NULL DEFAULT 0,
    total_rooms_managed INT NOT NULL DEFAULT 0,
    cpu_usage_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    memory_usage_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    open_critical_tickets INT NOT NULL DEFAULT 0,
    api_requests_per_minute INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_platform_tickets_tenant_code
    ON super_admin.platform_tickets(tenant_code);

CREATE INDEX IF NOT EXISTS idx_platform_tickets_status_priority
    ON super_admin.platform_tickets(status, priority);

CREATE INDEX IF NOT EXISTS idx_platform_audit_logs_created_at
    ON super_admin.platform_audit_logs(created_at);

CREATE INDEX IF NOT EXISTS idx_system_telemetry_snapshots_created_at
    ON super_admin.system_telemetry_snapshots(created_at);

-- Global Config Seed Insert
INSERT INTO super_admin.platform_configs (
    id,
    maintenance_mode,
    registration_open,
    max_tenants,
    session_timeout_minutes,
    telemetry_interval_seconds,
    backup_schedule,
    enforce_mfa,
    log_retention_days,
    default_timezone,
    created_at,
    updated_at
) VALUES (
    'GLOBAL_CONFIG',
    FALSE,
    TRUE,
    500,
    60,
    30,
    '0 2 * * *',
    FALSE,
    90,
    'UTC',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;
