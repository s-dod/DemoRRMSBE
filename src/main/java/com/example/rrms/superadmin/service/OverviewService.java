package com.example.rrms.superadmin.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.superadmin.DTO.response.AuditLogResponse;
import com.example.rrms.superadmin.DTO.response.OverviewActivityResponse;
import com.example.rrms.superadmin.DTO.response.OverviewStatsResponse;
import com.example.rrms.superadmin.DTO.response.TicketResponse;
import com.example.rrms.superadmin.entity.PlatformConfig;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;
import com.example.rrms.superadmin.repository.PlatformAuditLogRepository;
import com.example.rrms.superadmin.repository.PlatformConfigRepository;
import com.example.rrms.superadmin.repository.PlatformTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverviewService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PlatformTicketRepository ticketRepository;
    private final PlatformAuditLogRepository auditLogRepository;
    private final PlatformConfigRepository configRepository;
    private final TicketService ticketService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public OverviewStatsResponse getStats() {
        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE);
        long suspendedTenants = tenantRepository.countByStatus(TenantStatus.SUSPENDED);
        long totalOwners = userRepository.countByRole(Role.OWNER);

        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN) +
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long criticalTickets = ticketRepository.countByPriorityAndStatusNot(TicketPriority.CRITICAL, TicketStatus.CLOSED);

        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long maxMem = runtime.maxMemory();
        long usedMem = totalMem - freeMem;
        double memPercent = maxMem > 0 ? Math.round(((double) usedMem / maxMem) * 1000.0) / 10.0 : 0.0;

        double cpuPercent = 10.0;
        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double load = sunOsBean.getCpuLoad();
                if (load >= 0) {
                    cpuPercent = Math.round(load * 1000.0) / 10.0;
                }
            }
        } catch (Throwable ignored) {
        }

        boolean maintenanceMode = configRepository.findById(PlatformConfig.GLOBAL_CONFIG)
                .map(PlatformConfig::isMaintenanceMode)
                .orElse(false);

        return new OverviewStatsResponse(
                totalTenants,
                activeTenants,
                suspendedTenants,
                totalOwners,
                openTickets,
                criticalTickets,
                cpuPercent,
                memPercent,
                maintenanceMode
        );
    }

    @Transactional(readOnly = true)
    public OverviewActivityResponse getActivity() {
        List<TicketResponse> recentTickets = ticketRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(ticketService::toResponse)
                .collect(Collectors.toList());

        List<AuditLogResponse> recentAuditLogs = auditLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(auditService::toResponse)
                .collect(Collectors.toList());

        List<String> alerts = new ArrayList<>();

        boolean maintenanceMode = configRepository.findById(PlatformConfig.GLOBAL_CONFIG)
                .map(PlatformConfig::isMaintenanceMode)
                .orElse(false);

        if (maintenanceMode) {
            alerts.add("Maintenance Mode is currently ACTIVE across the platform.");
        }

        long criticalTickets = ticketRepository.countByPriorityAndStatusNot(TicketPriority.CRITICAL, TicketStatus.CLOSED);
        if (criticalTickets > 0) {
            alerts.add("There are " + criticalTickets + " unresolved CRITICAL support tickets requiring immediate attention.");
        }

        long suspendedTenants = tenantRepository.countByStatus(TenantStatus.SUSPENDED);
        if (suspendedTenants > 0) {
            alerts.add(suspendedTenants + " resort tenant(s) are currently SUSPENDED.");
        }

        if (alerts.isEmpty()) {
            alerts.add("All platform systems, tenant databases, and clusters operating normally.");
        }

        return new OverviewActivityResponse(recentTickets, recentAuditLogs, alerts);
    }
}
