package com.example.rrms.authsecurity.domain.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens",
        indexes = @Index(
                name = "idx_rt_hash",
                columnList = "tokenHash",
                unique = true))
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rt_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", foreignKey = @ForeignKey(name = "fk_rt_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 64)
    private String tokenHash; // SHA-256

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(UUID userId) {
        if (userId == null) {
            this.user = null;
        } else {
            User u = new User();
            u.setId(userId);
            this.user = u;
        }
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
}
