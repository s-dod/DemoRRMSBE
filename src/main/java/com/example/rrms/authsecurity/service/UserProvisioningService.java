package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.request.CreateTenantRequest;
import com.example.rrms.authsecurity.dto.request.CreateUserRequest;
import com.example.rrms.authsecurity.dto.response.CreatedUserResponse;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import com.example.rrms.authsecurity.security.user.PermissionRegistry;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    /** SUPER_ADMIN only: creates the resort (tenant) and its OWNER in one transaction. */
    @Transactional
    public CreatedUserResponse createTenantWithOwner(CreateTenantRequest createTenantRequest) {
        UserPrincipal creator = CurrentUser.get();
        if (!CreationPolicy.canCreate(creator, Role.OWNER)) throw new AccessDeniedException("Not allowed");

        String code = createTenantRequest.tenantCode() != null ? createTenantRequest.tenantCode().trim().toUpperCase() : "";
        if (code.isBlank()) {
            throw new IllegalArgumentException("Resort / Tenant code cannot be blank.");
        }
        if (tenantRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Resort / Tenant code '" + code + "' is already in use. Please choose a different code.");
        }

        String email = createTenantRequest.ownerEmail() != null ? createTenantRequest.ownerEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new IllegalArgumentException("Owner email cannot be blank.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        Tenant tenant = new Tenant();
        tenant.setCode(code);
        tenant.setName(createTenantRequest.resortName().trim());
        tenant.setTier(createTenantRequest.tier() != null ? createTenantRequest.tier() : "STARTER");
        tenant.setLocation(createTenantRequest.location() != null ? createTenantRequest.location().trim() : null);
        tenant.setRoomsCount(createTenantRequest.roomsCount() != null ? createTenantRequest.roomsCount() : 0);
        tenant.setCreatedBy(creator.getId());
        tenant = tenantRepository.save(tenant);

        return persistUser(tenant.getId(), createTenantRequest.ownerName().trim(), email, null, Role.OWNER, null, creator);
    }

    /** OWNER -> MANAGEMENT, MANAGEMENT -> STAFF/GUEST, RECEPTIONIST -> GUEST. Same tenant as creator. */
    @Transactional
    public CreatedUserResponse createUser(Role target, CreateUserRequest createUserRequest) {
        UserPrincipal creator = CurrentUser.get();

        if (!CreationPolicy.canCreate(creator, target)) {
            throw new AccessDeniedException("You cannot create " + target);
        }
        if (target == Role.STAFF && createUserRequest.staffType() == null) {
            throw new IllegalArgumentException("staffType is required for STAFF");
        }
        if (target != Role.STAFF && createUserRequest.staffType() != null) {
            throw new IllegalArgumentException("staffType is only valid for STAFF");
        }

        UUID tenantId = CurrentUser.tenantId();                     // <- from the token, never from the request
        User user = new User();
        modelMapper.map(createUserRequest, user);
        return persistUser(tenantId, user.getName(), user.getEmail(), user.getPhone(), target, createUserRequest.staffType(), creator);
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> getTenantRepository() {
        return tenantRepository.findAll().stream()
                .map(tenant -> {
                    User owner = userRepository.findByTenantIdAndRole(tenant.getId(), Role.OWNER).stream().findFirst().orElse(null);
                    return new TenantResponse(
                            tenant.getId(),
                            tenant.getCode(),
                            tenant.getName(),
                            tenant.getStatus(),
                            tenant.getTier(),
                            tenant.getLocation(),
                            tenant.getRoomsCount(),
                            tenant.getCreatedById(),
                            tenant.getCreatedAt(),
                            owner != null ? owner.getId() : null,
                            owner != null ? owner.getName() : "Unassigned",
                            owner != null ? owner.getEmail() : "N/A",
                            owner != null ? owner.getPhone() : null,
                            owner != null ? owner.getStatus() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        UUID tenantId = CurrentUser.tenantId();
        List<User> list = role == null ? userRepository.findByTenantId(tenantId) : userRepository.findByTenantIdAndRole(tenantId, role);
        return list.stream().map(this::mapUserResponse).collect(Collectors.toList());
    }

    private UserResponse mapUserResponse(User user) {
        Set<String> perms = PermissionRegistry.resolve(user.getRole(), user.getStaffType()).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        UserResponse response = modelMapper.map(user, UserResponse.class);
        return new UserResponse(
                response.id(), response.tenantId(), response.name(), response.email(), response.phone(),
                response.role(), response.staffType(), response.status(), response.mustChangePassword(),
                perms, response.createdAt()
        );
    }

    private CreatedUserResponse persistUser(UUID tenantId, String name, String email, String phone,
                                            Role role, StaffType type, UserPrincipal creator) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }
        String tempPassword = generateTempPassword();

        User user = new User();
        user.setTenantId(tenantId);
        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setPhone(phone);
        user.setRole(role);
        user.setStaffType(type);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);                              // forced reset on first login
        user.setCreatedBy(creator.getId());
        user = userRepository.save(user);

        return new CreatedUserResponse(user.getId(), user.getEmail(), role, tempPassword);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 14; i++) stringBuilder.append(chars.charAt(secureRandom.nextInt(chars.length())));
        return stringBuilder.toString();
    }
}
