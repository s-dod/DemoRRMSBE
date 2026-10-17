package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.modal.TechnicalTicket;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.request.CreateTechnicalTicketRequest;
import com.example.rrms.authsecurity.dto.request.UpdateTicketStatusRequest;
import com.example.rrms.authsecurity.dto.response.SystemMonitoringResponse;
import com.example.rrms.authsecurity.dto.response.TechnicalTicketResponse;
import com.example.rrms.authsecurity.repository.AuditLogRepository;
import com.example.rrms.authsecurity.repository.TechnicalTicketRepository;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformSupportService {

    private final TechnicalTicketRepository technicalTicketRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void seedInitialTicketsIfEmpty() {
        if (technicalTicketRepository.count() == 0) {
            Tenant palm = tenantRepository.findByCode("PALM").orElse(null);
            if (palm != null) {
                User owner = userRepository.findByTenantIdAndRole(palm.getId(), Role.OWNER).stream().findFirst().orElse(null);
                UUID ownerId = owner != null ? owner.getId() : null;
                String ownerName = owner != null ? owner.getName() : "Resort Owner";
                String ownerEmail = owner != null ? owner.getEmail() : "owner@rrms.demo";

                TechnicalTicket t1 = new TechnicalTicket();
                t1.setTenantId(palm.getId());
                t1.setTenantCode(palm.getCode());
                t1.setResortName(palm.getName());
                t1.setOwnerId(ownerId);
                t1.setOwnerName(ownerName);
                t1.setOwnerEmail(ownerEmail);
                t1.setTitle("Payment Gateway Webhook Timeout on Checkout");
                t1.setCategory("INTEGRATION");
                t1.setPriority("HIGH");
                t1.setStatus("OPEN");
                t1.setDescription("Payment confirmation webhooks from the merchant gateway are taking > 4.5 seconds during peak checkout times (11:00 AM - 12:30 PM). Please check upstream connection pooling.");
                technicalTicketRepository.save(t1);

                TechnicalTicket t2 = new TechnicalTicket();
                t2.setTenantId(palm.getId());
                t2.setTenantCode(palm.getCode());
                t2.setResortName(palm.getName());
                t2.setOwnerId(ownerId);
                t2.setOwnerName(ownerName);
                t2.setOwnerEmail(ownerEmail);
                t2.setTitle("Automated Nightly Database Snapshot Verification");
                t2.setCategory("DATABASE");
                t2.setPriority("MEDIUM");
                t2.setStatus("RESOLVED");
                t2.setDescription("Need confirmation that our nightly tenant schema snapshot at 03:00 UTC was replicated to disaster recovery storage successfully.");
                t2.setResolutionNotes("Verified: Disaster recovery replication confirmed for all active tenant shards with zero RPO degradation.");
                technicalTicketRepository.save(t2);

                log.info("Seeded initial platform technical tickets for Super Admin.");
            }
        }
    }

    @Transactional
    public TechnicalTicketResponse createOwnerTicket(CreateTechnicalTicketRequest createTechnicalTicketRequest) {
        UserPrincipal principal = CurrentUser.get();
        if (principal.getRole() != Role.OWNER && principal.getRole() != Role.MANAGEMENT) {
            throw new AccessDeniedException("Only resort owners or managers can raise technical tickets to Super Admin");
        }

        UUID tenantId = CurrentUser.tenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        TechnicalTicket ticket = new TechnicalTicket();
        modelMapper.map(createTechnicalTicketRequest, ticket);
        ticket.setTenantId(tenant.getId());
        ticket.setTenantCode(tenant.getCode());
        ticket.setResortName(tenant.getName());
        User currentUser = userRepository.findById(principal.getId()).orElse(null);
        String ownerName = currentUser != null && currentUser.getName() != null ? currentUser.getName() : "Resort Owner";
        ticket.setOwnerId(principal.getId());
        ticket.setOwnerName(ownerName);
        ticket.setOwnerEmail(principal.getEmail());
        ticket.setCategory(createTechnicalTicketRequest.category().trim().toUpperCase());
        ticket.setPriority(createTechnicalTicketRequest.priority().trim().toUpperCase());
        ticket.setStatus("OPEN");

        ticket = technicalTicketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TechnicalTicketResponse> getOwnerTickets() {
        UUID tenantId = CurrentUser.tenantId();
        return technicalTicketRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TechnicalTicketResponse> getAllPlatformTickets() {
        return technicalTicketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TechnicalTicketResponse updateTicketStatus(UUID id, UpdateTicketStatusRequest updateTicketStatusRequest) {
        TechnicalTicket ticket = technicalTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));

        ticket.setStatus(updateTicketStatusRequest.status().trim().toUpperCase());
        if (updateTicketStatusRequest.resolutionNotes() != null && !updateTicketStatusRequest.resolutionNotes().isBlank()) {
            ticket.setResolutionNotes(updateTicketStatusRequest.resolutionNotes().trim());
        }

        ticket = technicalTicketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional(readOnly = true)
    public SystemMonitoringResponse getSystemMonitoring() {
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        double usagePercent = maxMem > 0 ? (double) usedMem / maxMem * 100.0 : 0.0;

        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        int activeThreads = Thread.activeCount();
        int processors = runtime.availableProcessors();
        String javaVersion = System.getProperty("java.version");

        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.findAll().stream()
                .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
                .count();

        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        Map<String, Long> usersByRole = new HashMap<>();
        for (Role role : Role.values()) {
            long count = allUsers.stream().filter(user -> user.getRole() == role).count();
            usersByRole.put(role.name(), count);
        }

        List<TechnicalTicket> allTickets = technicalTicketRepository.findAll();
        long openTickets = allTickets.stream().filter(technicalTicket -> !"RESOLVED".equalsIgnoreCase(technicalTicket.getStatus())).count();
        long criticalTickets = allTickets.stream().filter(technicalTicket -> "CRITICAL".equalsIgnoreCase(technicalTicket.getPriority()) && !"RESOLVED".equalsIgnoreCase(technicalTicket.getStatus())).count();
        long auditEvents = auditLogRepository.count();

        return new SystemMonitoringResponse(
                "HEALTHY",
                uptimeSeconds,
                usedMem,
                freeMem,
                totalMem,
                maxMem,
                Math.round(usagePercent * 10.0) / 10.0,
                activeThreads,
                processors,
                javaVersion,
                totalTenants,
                activeTenants,
                totalUsers,
                usersByRole,
                openTickets,
                criticalTickets,
                auditEvents,
                "CONNECTED"
        );
    }

    private TechnicalTicketResponse mapToResponse(TechnicalTicket technicalTicket) {
        return modelMapper.map(technicalTicket, TechnicalTicketResponse.class);
    }
}
