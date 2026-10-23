package com.example.rrms.superadmin.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.common.exception.ApiException;
import com.example.rrms.superadmin.DTO.request.CreateTenantRequest;
import com.example.rrms.superadmin.DTO.request.TenantStatusRequest;
import com.example.rrms.superadmin.DTO.request.UpdateTenantRequest;
import com.example.rrms.superadmin.DTO.response.TenantDetailResponse;
import com.example.rrms.superadmin.enums.AuditSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TenantDetailResponse> getAll() {
        return tenantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TenantDetailResponse> getAll(Pageable pageable) {
        return tenantRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TenantDetailResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public TenantDetailResponse create(CreateTenantRequest req) {
        String email = req.ownerEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_ALREADY_IN_USE", "Owner email already in use: " + email);
        }

        String code = req.tenantCode() != null && !req.tenantCode().isBlank()
                ? req.tenantCode().trim().toUpperCase()
                : generateUniqueCode(req.resortName());

        if (tenantRepository.existsByCode(code)) {
            code = generateUniqueCode(req.resortName());
        }

        Tenant tenant = new Tenant();
        tenant.setCode(code);
        tenant.setName(req.resortName().trim());
        tenant.setAddress(req.address());
        tenant.setCity(req.city());
        tenant.setState(req.state());
        tenant.setPhone(req.resortPhone());
        tenant.setLocation((req.city() != null ? req.city() : "") + (req.state() != null ? ", " + req.state() : ""));
        tenant.setRoomsCount(req.roomsCount() != null ? req.roomsCount() : 0);
        tenant.setTier(req.tier() != null ? req.tier() : "STARTER");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);

        User owner = new User();
        owner.setTenantId(tenant.getId());
        owner.setName(req.ownerName().trim());
        owner.setEmail(email);
        owner.setPhone(req.ownerPhone());
        owner.setPasswordHash(passwordEncoder.encode(req.ownerPassword()));
        owner.setRole(Role.OWNER);
        owner.setStatus(UserStatus.ACTIVE);
        owner.setMustChangePassword(false);
        userRepository.save(owner);

        auditService.record(
                "SUPER_ADMIN",
                "TENANT_CREATE",
                "Created resort '" + tenant.getName() + "' (Code: " + tenant.getCode() + ") with owner " + owner.getEmail(),
                AuditSeverity.INFO,
                tenant.getCode(),
                null
        );

        return toResponse(tenant);
    }

    @Transactional
    public TenantDetailResponse update(UUID id, UpdateTenantRequest req) {
        Tenant tenant = findOrThrow(id);
        if (req.resortName() != null && !req.resortName().isBlank()) tenant.setName(req.resortName().trim());
        if (req.address() != null) tenant.setAddress(req.address().trim());
        if (req.city() != null)    tenant.setCity(req.city().trim());
        if (req.state() != null)   tenant.setState(req.state().trim());
        if (req.phone() != null)   tenant.setPhone(req.phone().trim());
        if (req.tier() != null)    tenant.setTier(req.tier().trim());
        if (req.roomsCount() != null) tenant.setRoomsCount(req.roomsCount());
        tenant.setLocation((tenant.getCity() != null ? tenant.getCity() : "") + (tenant.getState() != null ? ", " + tenant.getState() : ""));

        tenant = tenantRepository.save(tenant);

        auditService.record(
                "SUPER_ADMIN",
                "TENANT_UPDATE",
                "Updated resort details for '" + tenant.getName() + "'",
                AuditSeverity.INFO,
                tenant.getCode(),
                null
        );

        return toResponse(tenant);
    }

    @Transactional
    public TenantDetailResponse updateStatus(UUID id, TenantStatusRequest req) {
        Tenant tenant = findOrThrow(id);
        TenantStatus oldStatus = tenant.getStatus();
        tenant.setStatus(req.status());
        tenant = tenantRepository.save(tenant);

        auditService.record(
                "SUPER_ADMIN",
                "TENANT_STATUS_CHANGE",
                "Changed status for '" + tenant.getName() + "' from " + oldStatus + " to " + req.status() + (req.reason() != null ? ". Reason: " + req.reason() : ""),
                AuditSeverity.WARNING,
                tenant.getCode(),
                null
        );

        return toResponse(tenant);
    }

    private Tenant findOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("TENANT_NOT_FOUND", "Tenant not found with ID: " + id));
    }

    private String generateUniqueCode(String name) {
        String base = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (base.length() > 4) {
            base = base.substring(0, 4);
        } else if (base.isEmpty()) {
            base = "RES";
        }
        String code = base;
        int i = 1;
        while (tenantRepository.existsByCode(code)) {
            code = base + i++;
        }
        return code;
    }

    public TenantDetailResponse toResponse(Tenant t) {
        User owner = userRepository.findFirstByTenantIdAndRole(t.getId(), Role.OWNER).orElse(null);
        return new TenantDetailResponse(
                t.getId(),
                t.getName(),
                t.getCode(),
                t.getAddress(),
                t.getCity(),
                t.getState(),
                t.getPhone(),
                t.getStatus(),
                owner != null ? owner.getName() : null,
                owner != null ? owner.getEmail() : null,
                owner != null ? owner.getPhone() : null,
                t.getCreatedAt()
        );
    }
}
