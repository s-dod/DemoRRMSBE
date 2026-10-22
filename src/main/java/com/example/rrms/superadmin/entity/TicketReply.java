package com.example.rrms.superadmin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "ticket_replies", schema = "super_admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketReply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_reply_ticket"))
    private PlatformTicket ticket;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_role", nullable = false, length = 50)
    private String senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketAttachment> attachments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("timestamp")
    private Instant createdAt = Instant.now();

    public Instant getTimestamp() {
        return createdAt;
    }

    public List<TicketAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public void addAttachment(TicketAttachment attachment) {
        if (attachment != null) {
            this.attachments.add(attachment);
        }
    }

    public TicketReply(PlatformTicket ticket, UUID senderId, String senderName, String senderRole, String message, Instant createdAt) {
        this.ticket = ticket;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}

