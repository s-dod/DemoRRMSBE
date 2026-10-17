package com.example.rrms.authsecurity.security.user;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID tenantId;          // null => SUPER_ADMIN
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final StaffType staffType;
    private final boolean enabled;        // status ACTIVE && tenant ACTIVE
    private final boolean accountNonLocked;
    private final boolean mustChangePassword;
    private final int tokenVersion;
    private final Set<GrantedAuthority> authorities;

    private UserPrincipal(User user, boolean tenantActive) {
        this.id = user.getId();
        this.tenantId = user.getTenantId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.staffType = user.getStaffType();
        this.enabled = user.getStatus() == UserStatus.ACTIVE && tenantActive;
        this.accountNonLocked = user.getLockedUntil() == null || user.getLockedUntil().isBefore(Instant.now());
        this.mustChangePassword = user.isMustChangePassword();
        this.tokenVersion = user.getTokenVersion();

        Set<GrantedAuthority> auth = new HashSet<>();
        auth.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        if (staffType != null) {
            auth.add(new SimpleGrantedAuthority("TYPE_" + staffType.name()));
        }
        PermissionRegistry.resolve(role, staffType)
                .forEach(permission -> auth.add(new SimpleGrantedAuthority(permission.name())));
        this.authorities = Set.copyOf(auth);
    }

    public static UserPrincipal from(User user, boolean tenantActive) {
        return new UserPrincipal(user, tenantActive);
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
