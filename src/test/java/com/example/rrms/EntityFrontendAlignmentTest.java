package com.example.rrms;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.domain.modal.AuditLog;
import com.example.rrms.authsecurity.domain.modal.Task;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.response.MfaSetupResponse;
import com.example.rrms.superadmin.DTO.response.OwnerResponse;
import com.example.rrms.superadmin.DTO.response.TelemetryResponse;
import com.example.rrms.superadmin.DTO.response.TenantDetailResponse;
import com.example.rrms.superadmin.DTO.response.TicketDetailResponse;
import com.example.rrms.superadmin.DTO.response.TicketReplyResponse;
import com.example.rrms.superadmin.DTO.response.TicketResponse;
import com.example.rrms.superadmin.entity.PlatformConfig;
import com.example.rrms.superadmin.entity.PlatformTicket;
import com.example.rrms.superadmin.enums.AuditSeverity;
import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityFrontendAlignmentTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void tenantDetailResponseExposesFrontendAndLegacyFields() throws Exception {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        TenantDetailResponse dto = new TenantDetailResponse(
                id, "PALM", "The Palm Luxury Resort", TenantStatus.ACTIVE, "Enterprise",
                "Candolim, Goa · India", 30, "contact@palm.demo",
                "Candolim Beach Road", "Candolim", "Goa", "9876543210",
                createdBy, ownerId, "Vikram Oberoi", "owner@rrms.demo", "9876543210",
                Instant.now()
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(dto));
        assertEquals("PALM", json.get("code").asText());
        assertEquals("PALM", json.get("slug").asText());
        assertEquals("The Palm Luxury Resort", json.get("name").asText());
        assertEquals("The Palm Luxury Resort", json.get("resortName").asText());
        assertEquals("Active", json.get("status").asText());
        assertEquals("Enterprise", json.get("tier").asText());
        assertEquals("Candolim, Goa · India", json.get("location").asText());
        assertEquals(30, json.get("roomsCount").asInt());
        assertEquals("contact@palm.demo", json.get("resortEmail").asText());
        assertEquals(ownerId.toString(), json.get("ownerId").asText());
        assertEquals(createdBy.toString(), json.get("createdBy").asText());
    }

    @Test
    void ownerResponseIncludesAccountCompatibilityFields() throws Exception {
        OwnerResponse dto = new OwnerResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Palm", "PALM",
                "Vikram", "owner@rrms.demo", "9876543210", UserStatus.ACTIVE, Instant.now()
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(dto));
        assertTrue(json.get("active").asBoolean());
        assertEquals("Owner", json.get("module").asText());
        assertEquals("Owner", json.get("role").asText());
        assertEquals("", json.get("shift").asText());
    }

    @Test
    void ticketReplyAndResponseMatchFrontendNames() throws Exception {
        UUID id = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        TicketDetailResponse.TicketAttachmentResponse att = new TicketDetailResponse.TicketAttachmentResponse(
                UUID.randomUUID(), "err.png", "image/png", 1024L, null, "data:image/png;base64,AA==", Instant.now()
        );
        TicketReplyResponse reply = new TicketReplyResponse(
                id, senderId, "Admin", "SUPER_ADMIN", "Fixed", List.of(att), Instant.now()
        );

        JsonNode replyJson = objectMapper.readTree(objectMapper.writeValueAsString(reply));
        assertTrue(replyJson.has("timestamp"));
        assertTrue(replyJson.has("attachments"));
        assertEquals(1, replyJson.get("attachments").size());

        TicketResponse ticket = new TicketResponse(
                UUID.randomUUID(), "PALM", "Palm Resort", "Vikram", "owner@rrms.demo",
                UUID.randomUUID(), UUID.randomUUID(), "Admin", "admin@rrms.demo",
                "Webhook timeout", TicketCategory.PAYMENTS, TicketPriority.HIGH, TicketStatus.IN_PROGRESS,
                "Desc", "Notes", List.of(reply), List.of(att), List.of(), Instant.now(), Instant.now()
        );
        JsonNode ticketJson = objectMapper.readTree(objectMapper.writeValueAsString(ticket));
        assertEquals("Notes", ticketJson.get("resolutionNotes").asText());
        assertEquals(1, ticketJson.get("replies").size());
        assertEquals(1, ticketJson.get("attachments").size());
    }

    @Test
    void platformTicketSupportsFullStatusPriorityAndNotesUpdates() {
        PlatformTicket ticket = PlatformTicket.create(
                "PALM", "Palm Resort", "Vikram", "owner@rrms.demo",
                UUID.randomUUID(), "Gateway latency", TicketCategory.PAYMENTS,
                TicketPriority.LOW, "Seeing slow responses"
        );
        ticket.updatePriority(TicketPriority.HIGH, "admin", "SUPER_ADMIN");
        ticket.updateStatus(TicketStatus.IN_PROGRESS, "admin", "SUPER_ADMIN");
        ticket.updateResolutionNotes("Investigating upstream");

        assertEquals(TicketPriority.HIGH, ticket.getPriority());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals("Investigating upstream", ticket.getResolutionNotes());
    }

    @Test
    void platformConfigUpdatesAllOperationalSettings() {
        PlatformConfig config = PlatformConfig.createDefault();
        config.updateOperationalSettings(false, 75, 45, "Daily @ 02:00 UTC", 120, "Asia/Kolkata");

        assertFalse(config.isRegistrationOpen());
        assertEquals(75, config.getMaxTenants());
        assertEquals(45, config.getTelemetryIntervalSeconds());
        assertEquals("Daily @ 02:00 UTC", config.getBackupSchedule());
        assertEquals(120, config.getLogRetentionDays());
        assertEquals("Asia/Kolkata", config.getDefaultTimezone());
    }

    @Test
    void auditSeverityIncludesSecurityAlertAndTelemetryIncludesId() throws Exception {
        assertEquals(AuditSeverity.SECURITY_ALERT, AuditSeverity.valueOf("SECURITY_ALERT"));

        UUID snapId = UUID.randomUUID();
        TelemetryResponse telemetry = new TelemetryResponse(
                snapId, 4, 108, 12.5, 40.0, 1, 200, 3600L, 24, 4, 50L, Instant.now()
        );
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(telemetry));
        assertEquals(snapId.toString(), json.get("id").asText());
        assertTrue(json.has("timestamp"));
    }

    @Test
    void taskAndAuditLogSerializeFrontendCompatibleFields() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTenantId(tenantId);
        task.setTitle("Restock minibar");
        task.setDepartment(StaffType.FANDB);
        task.setAssignedStaffId(staffId);
        task.setRoomId("R101");
        task.setPriority("High");
        task.setDeadline("2026-11-12T16:00");
        task.setNotes("Extra water bottles");
        task.setKind("General");

        JsonNode taskJson = objectMapper.readTree(objectMapper.writeValueAsString(task));
        assertEquals("F&B", taskJson.get("role").asText());
        assertEquals(staffId.toString(), taskJson.get("assignee").asText());
        assertEquals("R101", taskJson.get("roomId").asText());
        assertEquals("High", taskJson.get("priority").asText());
        assertEquals("2026-11-12T16:00", taskJson.get("deadline").asText());
        assertEquals("Extra water bottles", taskJson.get("notes").asText());
        assertEquals("General", taskJson.get("kind").asText());
        assertFalse(taskJson.has("tenant"));

        AuditLog log = new AuditLog();
        log.setActor("Ananya Kapoor");
        log.setActorRole("MANAGEMENT");
        log.setAction("TASK_CREATE");
        log.setDetails("Created room task");
        JsonNode logJson = objectMapper.readTree(objectMapper.writeValueAsString(log));
        assertEquals("Ananya Kapoor", logJson.get("actor").asText());
        assertEquals("MANAGEMENT", logJson.get("role").asText());
        assertEquals("Created room task", logJson.get("detail").asText());
        assertTrue(logJson.has("date"));

        MfaSetupResponse mfa = new MfaSetupResponse("SECRET123", "otpauth://totp/RRMS");
        JsonNode mfaJson = objectMapper.readTree(objectMapper.writeValueAsString(mfa));
        assertEquals("SECRET123", mfaJson.get("secret").asText());
        assertEquals("otpauth://totp/RRMS", mfaJson.get("otpAuthUri").asText());

        User user = new User();
        user.setRole(Role.SUPER_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        assertEquals("SuperAdmin", user.getModule());
        assertTrue(user.isActive());

        Tenant tenant = new Tenant();
        tenant.setResortEmail("contact@palm.demo");
        assertEquals("contact@palm.demo", tenant.getResortEmail());
    }
}

