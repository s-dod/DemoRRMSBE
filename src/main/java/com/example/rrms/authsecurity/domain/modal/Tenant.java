package com.example.rrms.authsecurity.domain.modal;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants", uniqueConstraints = @UniqueConstraint(
        name = "uk_tenant_code",
        columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(length = 50)
    private String tier = "STARTER";

    @Column(length = 255)
    private String location;

    @Column(name = "rooms_count")
    private Integer roomsCount = 0;

    @Column(length = 255)
    private String address;

    @Column(length = 60)
    private String city;

    @Column(length = 60)
    private String state;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_tenant_creator"))
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

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
