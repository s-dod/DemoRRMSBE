package com.example.rrms.superadmin.entity;

import com.example.rrms.common.exception.ApiException;
import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "platform_tickets",
    schema = "super_admin",
    indexes = {
        @Index(name = "idx_platform_tickets_tenant_code", columnList = "tenant_code"),
        @Index(name = "idx_platform_tickets_status_priority", columnList = "status, priority")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformTicket extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_code", nullable = false, length = 50)
    private String tenantCode;

    @Column(name = "resort_name", nullable = false)
    private String resortName;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "assignee_name")
    private String assigneeName;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketReply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistoryEntry> history = new ArrayList<>();

    public static PlatformTicket create(String tenantCode, String resortName, String ownerName, String ownerEmail,
                                        UUID creatorId, String title, TicketCategory category,
                                        TicketPriority priority, String description) {
        if (title == null || title.trim().length() < 5) {
            throw ApiException.badRequest("INVALID_TITLE", "Title must be at least 5 characters long");
        }
        if (tenantCode == null || tenantCode.isBlank()) {
            throw ApiException.badRequest("INVALID_TENANT", "Tenant code is required");
        }
        if (creatorId == null) {
            throw ApiException.badRequest("INVALID_CREATOR", "Creator ID is required");
        }

        PlatformTicket ticket = new PlatformTicket();
        ticket.tenantCode = tenantCode;
        ticket.resortName = resortName != null ? resortName : "";
        ticket.ownerName = ownerName != null ? ownerName : "";
        ticket.ownerEmail = ownerEmail != null ? ownerEmail : "";
        ticket.creatorId = creatorId;
        ticket.title = title.trim();
        ticket.category = category != null ? category : TicketCategory.GENERAL_SUPPORT;
        ticket.priority = priority != null ? priority : TicketPriority.MEDIUM;
        ticket.status = TicketStatus.OPEN;
        ticket.description = description != null ? description : "";

        String actor = (ticket.ownerName == null || ticket.ownerName.isBlank()) ? "SYSTEM" : ticket.ownerName;
        ticket.addHistoryEntry(actor, "OWNER", "TICKET_CREATED", "Ticket created with title: " + ticket.title);
        return ticket;
    }

    public void assignAgent(UUID agentId, String agentName, String actorName, String actorRole) {
        this.assigneeId = agentId;
        this.assigneeName = agentName;
        this.status = TicketStatus.IN_PROGRESS;
        String actor = actorName != null ? actorName : (agentName != null ? agentName : "SUPER_ADMIN");
        String role = actorRole != null ? actorRole : "SUPER_ADMIN";
        addHistoryEntry(actor, role, "AGENT_ASSIGNED", "Ticket assigned to " + agentName + "; status updated to IN_PROGRESS");
    }

    public void assignAgent(UUID agentId, String agentName) {
        assignAgent(agentId, agentName, agentName, "SUPER_ADMIN");
    }

    public TicketReply addReply(UUID senderId, String senderName, String senderRole, String message) {
        TicketReply reply = new TicketReply(this, senderId, senderName, senderRole, message, Instant.now());
        this.replies.add(reply);
        addHistoryEntry(senderName, senderRole, "REPLY_ADDED", "Reply posted by " + senderName);
        return reply;
    }

    public TicketAttachment addAttachment(TicketReply reply, String fileName, String mimeType, long sizeBytes,
                                          String storageKey, String actorName, String actorRole) {
        TicketAttachment attachment = new TicketAttachment(this, reply, fileName, mimeType, sizeBytes, storageKey, Instant.now());
        this.attachments.add(attachment);
        String actor = actorName != null ? actorName : "USER";
        String role = actorRole != null ? actorRole : "USER";
        addHistoryEntry(actor, role, "ATTACHMENT_ADDED", "Attachment added: " + fileName);
        return attachment;
    }

    public TicketAttachment addAttachment(String fileName, String mimeType, long sizeBytes,
                                          String storageKey, String actorName, String actorRole) {
        return addAttachment(null, fileName, mimeType, sizeBytes, storageKey, actorName, actorRole);
    }

    public void escalateToCritical(String actorName, String actorRole) {
        this.priority = TicketPriority.CRITICAL;
        String actor = actorName != null ? actorName : "SUPER_ADMIN";
        String role = actorRole != null ? actorRole : "SUPER_ADMIN";
        addHistoryEntry(actor, role, "PRIORITY_ESCALATED", "Ticket priority escalated to CRITICAL");
    }

    public void escalateToCritical() {
        escalateToCritical("SUPER_ADMIN", "SUPER_ADMIN");
    }

    public void resolve(String resolutionNotes, String actorName, String actorRole) {
        if (this.status == TicketStatus.CLOSED) {
            throw ApiException.conflict("TICKET_ALREADY_CLOSED", "Cannot resolve a ticket that has already been closed");
        }
        this.status = TicketStatus.RESOLVED;
        this.resolutionNotes = resolutionNotes;
        String actor = actorName != null ? actorName : "SUPER_ADMIN";
        String role = actorRole != null ? actorRole : "SUPER_ADMIN";
        addHistoryEntry(actor, role, "TICKET_RESOLVED", "Ticket resolved. Notes: " + (resolutionNotes != null ? resolutionNotes : "None"));
    }

    public void resolve(String resolutionNotes) {
        resolve(resolutionNotes, "SUPER_ADMIN", "SUPER_ADMIN");
    }

    public void close(String actorName, String actorRole) {
        this.status = TicketStatus.CLOSED;
        String actor = actorName != null ? actorName : "SUPER_ADMIN";
        String role = actorRole != null ? actorRole : "SUPER_ADMIN";
        addHistoryEntry(actor, role, "TICKET_CLOSED", "Ticket closed");
    }

    public void close() {
        close("SUPER_ADMIN", "SUPER_ADMIN");
    }

    private void addHistoryEntry(String actorName, String actorRole, String action, String details) {
        TicketHistoryEntry entry = new TicketHistoryEntry(
            this,
            actorName != null ? actorName : "SYSTEM",
            actorRole != null ? actorRole : "SYSTEM",
            action,
            details,
            Instant.now()
        );
        this.history.add(entry);
    }

    public List<TicketReply> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public List<TicketAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public List<TicketHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
