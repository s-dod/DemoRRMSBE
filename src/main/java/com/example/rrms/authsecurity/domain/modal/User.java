package com.example.rrms.authsecurity.domain.modal;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_user_email",
               columnNames = {"email"}),
       indexes = @Index(
               name = "idx_user_tenant",
               columnList = "tenant_id"))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", foreignKey = @ForeignKey(name = "fk_user_tenant"))
    private Tenant tenant; // null only for SUPER_ADMIN

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash; // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private StaffType staffType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private String phone;

    @Column(length = 50)
    private String shift;

    @Column(name = "guest_id", length = 100)
    private String guestId;

    private boolean mustChangePassword = true;

    private boolean mfaEnabled;

    @JsonIgnore
    private String mfaSecret;

    private boolean mfaConfirmed;

    private int failedAttempts;

    private Instant lockedUntil;

    private int tokenVersion = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_user_creator"))
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public String getModule() {
        if (role == null) return null;
        return switch (role) {
            case SUPER_ADMIN -> "SuperAdmin";
            case OWNER -> "Owner";
            case MANAGEMENT -> "Management";
            case STAFF -> "Staff";
            case GUEST -> "Guest";
        };
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

    public UUID getCreatedById() {
        return createdBy != null ? createdBy.getId() : null;
    }

    public void setCreatedBy(UUID userId) {
        if (userId == null) {
            this.createdBy = null;
        } else {
            User u = new User();
            u.setId(userId);
            this.createdBy = u;
        }
    }
}
