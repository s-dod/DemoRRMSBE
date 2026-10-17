package com.example.rrms.superadmin.entity;

import com.example.rrms.superadmin.enums.SuperAdminRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "super_admin_users",
    schema = "super_admin",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_super_admin_user_email", columnNames = {"email"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuperAdminUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SuperAdminRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "mfa_required", nullable = false)
    private boolean mfaRequired = false;

    @Column(name = "mfa_secret", length = 500)
    private String mfaSecret;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public static SuperAdminUser create(String name, String email, String passwordHash, SuperAdminRole role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        SuperAdminUser user = new SuperAdminUser();
        user.name = name;
        user.email = email.toLowerCase().trim();
        user.passwordHash = passwordHash;
        user.role = role;
        user.active = true;
        user.mfaRequired = false;
        return user;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public void enableMfa(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("MFA secret cannot be null or blank");
        }
        this.mfaSecret = secret;
        this.mfaRequired = true;
    }

    public void disableMfa() {
        this.mfaSecret = null;
        this.mfaRequired = false;
    }

    public void deactivate() {
        this.active = false;
    }
}
