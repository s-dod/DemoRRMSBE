package com.example.rrms.superadmin.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.service.RefreshTokenService;
import com.example.rrms.common.exception.ApiException;
import com.example.rrms.superadmin.DTO.request.UpdateOwnerRequest;
import com.example.rrms.superadmin.DTO.response.OwnerResponse;
import com.example.rrms.superadmin.enums.AuditSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<OwnerResponse> getAll() {
        return userRepository.findByRole(Role.OWNER).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OwnerResponse getById(UUID id) {
        User user = findOwnerOrThrow(id);
        return toResponse(user);
    }

    @Transactional
    public OwnerResponse update(UUID id, UpdateOwnerRequest req) {
        User owner = findOwnerOrThrow(id);

        if (req.email() != null && !req.email().isBlank()) {
            String newEmail = req.email().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(owner.getEmail())) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw ApiException.conflict("EMAIL_ALREADY_IN_USE", "Email already in use: " + newEmail);
                }
                owner.setEmail(newEmail);
            }
        }

        if (req.name() != null && !req.name().isBlank()) {
            owner.setName(req.name().trim());
        }

        if (req.phone() != null) {
            owner.setPhone(req.phone().trim());
        }

        if (req.status() != null) {
            owner.setStatus(req.status());
        }

        owner = userRepository.save(owner);

        auditService.record(
                "SUPER_ADMIN",
                "OWNER_PROFILE_UPDATE",
                "Updated Owner profile for '" + owner.getName() + "' (" + owner.getEmail() + ")",
                AuditSeverity.INFO,
                owner.getTenant() != null ? owner.getTenant().getCode() : null,
                null
        );

        return toResponse(owner);
    }

    @Transactional
    public Map<String, Object> resetPassword(UUID id) {
        User owner = findOwnerOrThrow(id);

        String tempPassword = generateSecureTempPassword();
        owner.setPasswordHash(passwordEncoder.encode(tempPassword));
        owner.setMustChangePassword(true);
        owner.setTokenVersion(owner.getTokenVersion() + 1);
        refreshTokenService.revokeAll(owner.getId());
        userRepository.save(owner);

        auditService.record(
                "SUPER_ADMIN",
                "OWNER_PASSWORD_RESET",
                "Reset password for Owner '" + owner.getName() + "' (" + owner.getEmail() + ")",
                AuditSeverity.WARNING,
                owner.getTenant() != null ? owner.getTenant().getCode() : null,
                null
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Owner password has been reset successfully.");
        response.put("ownerId", owner.getId());
        response.put("email", owner.getEmail());
        response.put("temporaryPassword", tempPassword);
        response.put("mustChangePassword", true);
        return response;
    }

    private User findOwnerOrThrow(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("OWNER_NOT_FOUND", "Owner not found with ID: " + id));

        if (user.getRole() != Role.OWNER) {
            throw ApiException.badRequest("NOT_AN_OWNER", "User with ID " + id + " is not an Owner");
        }
        return user;
    }

    public OwnerResponse toResponse(User user) {
        Tenant tenant = null;
        if (user.getTenantId() != null) {
            tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
        }
        return new OwnerResponse(
                user.getId(),
                user.getTenantId(),
                tenant != null ? tenant.getName() : null,
                tenant != null ? tenant.getCode() : null,
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private String generateSecureTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("Tmp@");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append("!");
        return sb.toString();
    }
}
