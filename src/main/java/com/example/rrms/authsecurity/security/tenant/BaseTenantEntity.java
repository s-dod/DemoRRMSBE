package com.example.rrms.authsecurity.security.tenant;

import com.example.rrms.authsecurity.domain.modal.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@FilterDef(name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public abstract class BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

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

    @PrePersist
    void requireTenant() {
        if (tenant == null || tenant.getId() == null) {
            throw new IllegalStateException("tenantId must be set before saving " + getClass().getSimpleName());
        }
    }
}
