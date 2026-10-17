package com.example.rrms.authsecurity.domain.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "technical_tickets")
@Getter
@Setter
@NoArgsConstructor
public class TechnicalTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 50)
    private String tenantCode;

    @Column(nullable = false)
    private String resortName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_owner"))
    private User owner;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 50)
    private String category; // SYSTEM, DATABASE, INTEGRATION, BILLING, PERFORMANCE, SECURITY

    @Column(nullable = false, length = 50)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, length = 50)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getTenantId() {
        return tenant != null ? tenant.getId() : null;
    }

    public void setTenantId(UUID tenantId) {
        if (tenantId == null) {
            this.tenant = null;
        } else {
            Tenant t = new Tenant();
            t.setId(tenantId);
            this.tenant = t;
        }
    }

    public UUID getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    public void setOwnerId(UUID ownerId) {
        if (ownerId == null) {
            this.owner = null;
        } else {
            User u = new User();
            u.setId(ownerId);
            this.owner = u;
        }
    }
}
